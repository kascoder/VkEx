package org.kascoder.vkex.core.service;

import org.kascoder.vkex.core.model.Conversation;
import org.kascoder.vkex.core.model.ConversationList;
import org.kascoder.vkex.core.model.Message;
import org.kascoder.vkex.core.model.Principal;
import org.kascoder.vkex.core.model.attachment.Video;
import org.kascoder.vkex.core.model.options.SearchOptions;
import lombok.NonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface VkClientMiddleware {
    Principal authenticate(String username, String password);
    List<Video> fetchVideoInfos(@NonNull Principal principal, @NonNull List<Video> videos);
    List<Message> searchForMessages(@NonNull Principal principal, @NonNull SearchOptions options);
    CompletableFuture<ConversationList> loadConversationList(Principal principal, int offset, int count);
    ConversationList loadConversationList(Principal principal);
    CompletableFuture<List<Conversation>> loadConversations(Principal principal, Set<Integer> conversationIdSet);
}
