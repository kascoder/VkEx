package org.kascoder.vkex.core.service;

import com.google.inject.Inject;
import io.kascoder.vkclient.VkApi;
import io.kascoder.vkclient.VkApiClient;
import io.kascoder.vkclient.domain.model.Community;
import io.kascoder.vkclient.domain.model.User;
import io.kascoder.vkclient.methods.user.messages.query.GetConversationsByIdQuery;
import io.kascoder.vkclient.methods.user.messages.query.GetConversationsQuery;
import io.kascoder.vkclient.methods.user.messages.query.GetHistoryQuery;
import io.kascoder.vkclient.methods.user.messages.response.HistoryMessageListResponse;
import io.kascoder.vkclient.methods.user.video.query.GetQuery;
import io.kascoder.vkclient.oauth.OAuth;
import io.kascoder.vkclient.util.VideoID;
import org.kascoder.vkex.core.model.*;
import org.kascoder.vkex.core.model.attachment.Video;
import org.kascoder.vkex.core.model.options.SearchOptions;
import org.kascoder.vkex.core.util.CoreUtils;
import org.kascoder.vkex.core.util.ModelConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class DefaultVkClientMiddleware implements VkClientMiddleware {
    private final Integer clientId;
    private final String clientSecret;

    @Inject
    public DefaultVkClientMiddleware(@Named("vk.client-id") Integer clientId,
                                     @Named("vk.client-secret") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Principal authenticate(String username, String password) {
        LOGGER.info("Trying to authenticate user with username '{}' using password flow...", username);
        VkApiClient vkApiClient = VkApiClient.byOAuth(new OAuth.User.PasswordFlow(clientId, clientSecret, username, password));
        var userId = vkApiClient.getUserId();
        LOGGER.info("User with id:{} and username:{} successfully authenticated", userId, username);
        return new Principal(userId, vkApiClient.getAccessToken());
    }

    @Override
    public List<Video> fetchVideoInfos(@NonNull Principal principal, @NonNull List<Video> videos) {
        var client = buildApiClient(principal);

        final var limit = 200;
        final var count = videos.size();
        final var partCount = (int) Math.ceil(count * 1.0 / limit);

        final var result = new ArrayList<Video>();
        for (int i = 0; i < partCount; i++) {
            var toIndex = (i + 1) * limit;
            if (toIndex > videos.size()) {
                toIndex = videos.size();
            }
            final var part = videos.subList(i * limit, toIndex);
            final var videoIDSet = part.stream()
                    .map(video -> VideoID.of(video.getOwnerId(), video.getId(), video.getAccessKey()))
                    .collect(toSet());
            var query = GetQuery.builder()
                    .videoIdSet(videoIDSet)
                    .count(limit)
                    .build();
            try {
                final var response = client.executeRequest(VkApi.video.get(query)).get();
                response.getVideoList()
                        .stream()
                        .map(ModelConverter::convert)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(result::add);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

            simulatePause();
        }

        return result;
    }

    @Override
    public List<Message> searchForMessages(@NonNull Principal principal, @NonNull SearchOptions options) {
        List<Message> messages = new ArrayList<>();
        var client = buildApiClient(principal);

        Predicate<LocalDateTime> isAfterFromDate = date -> options.getFrom() == null || date.compareTo(options.getFrom()) >= 0;
        Predicate<LocalDateTime> isBeforeToDate = date -> options.getTo() == null || date.compareTo(options.getTo()) < 0;

        final int limit = 200;
        Integer total = null;
        int offset = 0, startMessageId = options.getStartMessageId();
        do {
            var query = GetHistoryQuery.builder()
                    .peerId(options.getConversationId())
                    .count(limit)
                    .startMessageId(startMessageId)
                    .offset(-limit)
                    .build();
            HistoryMessageListResponse messageHistory;
            try {
                messageHistory = client.executeRequest(VkApi.messages.getHistory(query))
                        .get();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

            if (total == null) {
                total = messageHistory.getTotal();
            }

            var lastMessageValue = messageHistory.getMessages()
                    .stream()
                    .map(ModelConverter::convert)
                    .sorted(Comparator.comparing(Message::getDate))
                    .peek(message -> {
                        var messageDate = message.getDate();
                        if (isAfterFromDate.and(isBeforeToDate).test(messageDate) && message.hasAttachments()) {
                            if (message.hasVideos()) {
                                // TODO maybe also load DOCUMENT_VIDEO
                                var videos = message.getAttachmentList()
                                        .stream()
                                        .filter(Video.class::isInstance)
                                        .map(Video.class::cast)
                                        .collect(toList());
                                LOGGER.info("Loading additional video information...");
                                var videoInfos = fetchVideoInfos(principal, videos);
                                LOGGER.info("Additional video information was loaded");
                                var videoIdVideoInfoMap = videoInfos.stream()
                                        .collect(Collectors.toMap(Video::getId, Function.identity(), (v1, v2) -> v1));
                                videos.forEach(video -> {
                                    var videoId = video.getId();
                                    var videoInfo = videoIdVideoInfoMap.get(videoId);
                                    if (videoInfo == null) {
                                        return;
                                    }

                                    video.setId(videoInfo.getId());
                                    video.setUrl(videoInfo.getUrl());
                                    video.setDate(videoInfo.getDate());
                                    video.setTitle(videoInfo.getTitle());
                                    video.setWidth(videoInfo.getWidth());
                                    video.setHeight(videoInfo.getHeight());
                                    video.setLive(videoInfo.isLive());
                                    video.setOwnerId(videoInfo.getOwnerId());
                                    video.setPlatform(videoInfo.getPlatform());
                                    video.setAccessKey(videoInfo.getAccessKey());
                                    video.setPlayerUrl(videoInfo.getPlayerUrl());
                                    video.setDescription(videoInfo.getDescription());
                                    video.setProcessing(videoInfo.isProcessing());
                                });

                                simulatePause();
                            }

                            messages.add(message);
                        }
                    })
                    .reduce((m1, m2) -> m2);

            if (lastMessageValue.isEmpty()) {
                break;
            }

            var lastMessage = lastMessageValue.get();
            if (Predicate.not(isBeforeToDate).test(lastMessage.getDate())) {
                break;
            }

            offset += limit;
            startMessageId = lastMessage.getId();

            simulatePause();
        } while (total != null && offset < total);

        return messages;
    }

    @Override
    public CompletableFuture<ConversationList> loadConversationList(Principal principal, int offset, int count) {
        LOGGER.info("User with id {} is loading conversations, offset={}, count={}", principal.getId(), offset, count);

        var vkApiClient = buildApiClient(principal);
        var query = GetConversationsQuery.builder()
                .offset(offset)
                .count(count)
                .extended(true)
                .build();

        return vkApiClient.executeRequest(VkApi.messages.getConversations(query))
                .thenApply(conversationListResponse -> {
                    Function<UserProfile, String> convertProfileToTitle = u -> u.getFirstName() + " " + u.getLastName();
                    Function<User, UserProfile> convertUserToProfile = u -> {
                        var id = u.getId();
                        var firstName = u.getFirstName();
                        var lastName = u.getLastName();

                        return new UserProfile(id, firstName, lastName);
                    };

                    var profiles = conversationListResponse.getProfiles()
                            .stream()
                            .collect(Collectors.toMap(User::getId, convertUserToProfile));

                    var communities = conversationListResponse.getCommunities();
                    Map<Integer, String> communityMap = communities == null ? Collections.emptyMap() : communities
                            .stream()
                            .collect(Collectors.toMap(Community::getId, Community::getName));

                    var items = conversationListResponse.getHistoryItems()
                            .stream()
                            .map(item -> {
                                var conversation = item.getConversation();
                                Integer lastMessageId = null;
                                if (item.getLastMessage() != null) {
                                    lastMessageId = item.getLastMessage().getId();
                                }
                                Integer id = conversation.getPeer().getId();
                                if (conversation.getChatSettings() != null) {
                                    var title = conversation.getChatSettings().getTitle();
                                    var memberCount = conversation.getChatSettings().getMemberCount();
                                    return new Conversation(id, title, memberCount, lastMessageId, profiles);
                                }
                                var profile = profiles.get(id);
                                if (profile != null) {
                                    var conversationTitle = convertProfileToTitle.apply(profile);
                                    return new Conversation(id, conversationTitle, 2, lastMessageId, profiles);
                                }
                                var communityTitle = Objects.requireNonNullElse(communityMap.get(id * -1), "Unknown");
                                return new Conversation(id, communityTitle, 2, lastMessageId, profiles);

                            })
                            .collect(toList());

                    return new ConversationList(conversationListResponse.getTotal(), items);
                });
    }

    @Override
    public ConversationList loadConversationList(Principal principal) {
        LOGGER.info("User with id {} is loading all conversations", principal.getId());

        int offset = 0;
        final int limit = 200;
        ConversationList resultList = null;
        do {
            ConversationList conversationList;
            try {
                if (resultList != null) {
                    simulatePause();
                }
                conversationList = loadConversationList(principal, offset, limit).get();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }

            if (resultList == null) {
                resultList = new ConversationList(conversationList.getTotal(), conversationList.getItems());
            } else {
                resultList.getItems().addAll(conversationList.getItems());
            }

            offset += limit;
        } while (offset < resultList.getTotal());

        return resultList;
    }

    @Override
    public CompletableFuture<List<Conversation>> loadConversations(Principal principal, Set<Integer> conversationIdSet) {
        LOGGER.info("User with id {} is loading conversations", principal.getId());

        var vkApiClient = buildApiClient(principal);
        var query = GetConversationsByIdQuery.builder()
                .peerIdSet(conversationIdSet)
                .extended(true)
                .build();

        return vkApiClient.executeRequest(VkApi.messages.getConversationsById(query))
                .thenApply(response -> {
                    Function<UserProfile, String> convertProfileToTitle = u -> u.getFirstName() + " " + u.getLastName();
                    Function<User, UserProfile> convertUserToProfile = u -> {
                        var id = u.getId();
                        var firstName = u.getFirstName();
                        var lastName = u.getLastName();

                        return new UserProfile(id, firstName, lastName);
                    };

                    var profiles = response.getProfiles()
                            .stream()
                            .collect(Collectors.toMap(User::getId, convertUserToProfile));

                    var communities = response.getCommunities();
                    Map<Integer, String> communityMap = communities == null ? Collections.emptyMap() : communities
                            .stream()
                            .collect(Collectors.toMap(Community::getId, Community::getName));

                    return response.getConversations()
                            .stream()
                            .map(conversation -> {
                                Integer lastMessageId = null;
                                Integer id = conversation.getPeer().getId();
                                if (conversation.getChatSettings() != null) {
                                    var title = conversation.getChatSettings().getTitle();
                                    var memberCount = conversation.getChatSettings().getMemberCount();
                                    return new Conversation(id, title, memberCount, lastMessageId, profiles);
                                }
                                var profile = profiles.get(id);
                                if (profile != null) {
                                    var conversationTitle = convertProfileToTitle.apply(profile);
                                    return new Conversation(id, conversationTitle, 2, lastMessageId, profiles);
                                }
                                var communityTitle = Objects.requireNonNullElse(communityMap.get(id * -1), "Unknown");
                                return new Conversation(id, communityTitle, 2, lastMessageId, profiles);

                            })
                            .collect(toList());
                });
    }

    private VkApiClient buildApiClient(Principal principal) {
        return VkApiClient.builder()
                .accessToken(principal.getAccessToken())
                .userId(principal.getId())
                .build();
    }

    private void simulatePause() {
        CoreUtils.sleep(400);
    }
}
