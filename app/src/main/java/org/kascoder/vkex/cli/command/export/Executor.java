package org.kascoder.vkex.cli.command.export;

import org.kascoder.vkex.cli.model.ExportFailure;
import org.kascoder.vkex.cli.model.ExportHistory;
import org.kascoder.vkex.cli.model.ExportHistoryItem;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.Conversation;
import org.kascoder.vkex.core.model.ConversationList;
import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.Message;
import org.kascoder.vkex.core.model.attachment.Attachment;
import org.kascoder.vkex.core.model.attachment.AttachmentType;
import org.kascoder.vkex.core.model.attachment.download.SuccessfulAttachmentDownloadResult;
import org.kascoder.vkex.core.model.options.ExportOptions;
import org.kascoder.vkex.core.model.options.SearchOptions;
import org.kascoder.vkex.core.model.source.SimpleMessageSource;
import org.kascoder.vkex.core.service.VkClientMiddleware;
import org.kascoder.vkex.core.service.export.ConversationExportService;
import org.kascoder.vkex.core.service.io.ContentService;
import org.kascoder.vkex.core.service.io.attachment.AttachmentDownloadServiceFactory;
import org.kascoder.vkex.core.util.CoreUtils;
import org.kascoder.vkex.core.util.ExportContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

@Slf4j
class Executor {
    public static final int TIMEOUT_BETWEEN_API_REQUESTS = 400;

    private final ContentService contentService;
    private final ApplicationContext applicationContext;
    private final VkClientMiddleware vkClientMiddleware;
    private final ConversationExportService conversationExportService;

    public Executor(ContentService contentService,
                    ApplicationContext applicationContext,
                    VkClientMiddleware vkClientMiddleware,
                    ConversationExportService conversationExportService) {
        this.contentService = contentService;
        this.applicationContext = applicationContext;
        this.vkClientMiddleware = vkClientMiddleware;
        this.conversationExportService = conversationExportService;
    }

    public void execute(String exportHistoryPath, @NonNull Set<Integer> conversationIdSet, BigDecimal totalSizeLimit) {
        boolean useHistory = StringUtils.isNotBlank(exportHistoryPath);
        List<ExportFailure> failures = new ArrayList<>();
        var history = new HashMap<Integer, ExportHistoryItem>();

        BigDecimal exportedSize = new BigDecimal(0);
        if (useHistory) {
            var exportHistoryValue = contentService.read(exportHistoryPath, ExportHistory.class);
            if (exportHistoryValue.isPresent()) {
                var exportHistory = exportHistoryValue.get();
                exportHistory.getItems()
                        .forEach(item -> history.put(item.getConversationId(), item));
                if (exportHistory.hasFailures()) {
                    var failuresTotal = exportHistory.getFailures().size();
                    LOGGER.warn("Found {} failures from the previous export", failuresTotal);
                    var preferences = applicationContext.getUserPreferences();
                    @NonNull var exportTypes = preferences.getExportTypes();

                    var downloadService = AttachmentDownloadServiceFactory.build(preferences.getStorageOptions());
                    downloadService.prepareEnvironment();
                    LOGGER.warn("Resolving failures...");
                    for (ExportFailure exportFailure : exportHistory.getFailures()) {
                        if (isSizeLimitExceed(exportedSize, totalSizeLimit)
                                || (!conversationIdSet.isEmpty() && !conversationIdSet.contains(exportFailure.getConversationId()))
                                || !exportTypes.contains(ExportType.from(exportFailure.getAttachment()))) {
                            failures.add(exportFailure);
                            continue;
                        }

                        var result = downloadService.downloadAttachment(exportFailure.getAttachment());
                        if (result.isSuccessful()) {
                            var downloadResult = (SuccessfulAttachmentDownloadResult) result;
                            var file = downloadResult.getFile();
                            if (file != null) {
                                exportedSize = exportedSize.add(new BigDecimal(file.length()));
                            }
                        } else {
                            failures.add(exportFailure);
                        }
                    }
                    downloadService.cleanUpEnvironment();
                    LOGGER.warn("Resolving completed. {} failures out of {} were resolved.", (failuresTotal - failures.size()), failuresTotal);
                }
            }
        }

        if (!isSizeLimitExceed(exportedSize, totalSizeLimit)) {
            try {
                export(conversationIdSet, history, failures, totalSizeLimit == null ? totalSizeLimit : totalSizeLimit.subtract(exportedSize));
            } catch (Exception e) {
                if (useHistory) {
                    saveHistory(exportHistoryPath, history, failures);
                }
                throw new RuntimeException(e.getMessage());
            }
        } else {
            LOGGER.warn("Total export size limit was exceeded");
        }

        if (useHistory) {
            saveHistory(exportHistoryPath, history, failures);
        }
    }

    public void export(@NonNull Set<Integer> conversationIdSet, Map<Integer, ExportHistoryItem> history, List<ExportFailure> failures, BigDecimal exportSizeLimit) {
        var principal = applicationContext.getPrincipal();
        var userPreferences = applicationContext.getUserPreferences();
        var totalSizeLimit = exportSizeLimit;

        var conversations = loadConversations(conversationIdSet);
        final int total = conversations.size();

        var attachmentDownloadService = AttachmentDownloadServiceFactory.build(userPreferences.getStorageOptions());

        LOGGER.warn("{} conversations were found", total);
        for (int i = 0; i < total; i++) {
            if (totalSizeLimit != null) {
                LOGGER.warn("Remaining export memory: {}Mb", convertBytesToMegabytes(totalSizeLimit));

                if (totalSizeLimit.compareTo(BigDecimal.ZERO) <= 0) {
                    LOGGER.warn("Total export size limit was exceeded");
                    break;
                }
            }

            var conversation = conversations.get(i);
            LOGGER.warn("Conversation {}/{}", (i + 1), total);
            LOGGER.warn("*********************************************************************************************");
            LOGGER.warn("Conversation id: {}", conversation.getId());

            var searchOptions = new SearchOptions();
            searchOptions.setConversationId(conversation.getId());

            LocalDateTime dateTime = null;
            var historyItem = history.get(conversation.getId());
            if (historyItem != null) {
                dateTime = historyItem.getLastExportedMessageDateTime();
                searchOptions.setStartMessageId(historyItem.getLastExportedMessageId());
            }

            if (dateTime == null) {
                LOGGER.warn("Searching for attachments from the beginning of the conversation");
            } else {
                searchOptions.setFrom(dateTime);
                LOGGER.warn("Searching for attachments from date: {}", dateTime);
            }

            List<Message> messages;
            try {
                messages = vkClientMiddleware.searchForMessages(principal, searchOptions);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.warn("*********************************************************************************************");
                continue;
            }

            if (messages.isEmpty()) {
                LOGGER.warn("Attachments weren't found");
                CoreUtils.sleep(TIMEOUT_BETWEEN_API_REQUESTS);
                LOGGER.warn("*********************************************************************************************");
                continue;
            }

            var lastMessage = messages.get(messages.size() - 1);
            messages = messages.stream()
                    .filter(Message::hasAttachments)
                    .collect(toList());
            if (messages.isEmpty()) {
                LOGGER.warn("Attachments weren't found");
                var item = new ExportHistoryItem(conversation.getId(), lastMessage.getId(), lastMessage.getDate().plusSeconds(1));
                history.put(conversation.getId(), item);
                CoreUtils.sleep(TIMEOUT_BETWEEN_API_REQUESTS);
                LOGGER.warn("*********************************************************************************************");
                continue;
            }

            var attachments = messages.stream()
                    .map(Message::getAttachmentList)
                    .flatMap(Collection::stream)
                    .collect(groupingBy(Attachment::getType));
            var attachmentCount = attachments.values()
                    .stream()
                    .mapToInt(Collection::size)
                    .sum();
            var photosCount = attachments.getOrDefault(AttachmentType.PHOTO, emptyList()).size()
                    + attachments.getOrDefault(AttachmentType.DOCUMENT_IMAGE, emptyList()).size();
            var gifsCount = attachments.getOrDefault(AttachmentType.DOCUMENT_GIF, emptyList()).size();
            var videosCount = attachments.getOrDefault(AttachmentType.VIDEO, emptyList()).size()
                    + attachments.getOrDefault(AttachmentType.DOCUMENT_VIDEO, emptyList()).size();
            var audiosCount = attachments.getOrDefault(AttachmentType.AUDIO, emptyList()).size()
                    + attachments.getOrDefault(AttachmentType.DOCUMENT_AUDIO, emptyList()).size();
            var audioMessagesCount = attachments.getOrDefault(AttachmentType.AUDIO_MESSAGE, emptyList()).size();
            var archivesCount = attachments.getOrDefault(AttachmentType.DOCUMENT_ARCHIVE, emptyList()).size();
            var documentsCount = attachments.getOrDefault(AttachmentType.DOCUMENT_EBOOK, emptyList()).size()
                    + attachments.getOrDefault(AttachmentType.DOCUMENT_UNKNOWN, emptyList()).size()
                    + attachments.getOrDefault(AttachmentType.DOCUMENT_TEXT, emptyList()).size();

            LOGGER.warn(
                    "{} attachments were found: {} photos, {} videos, {} audios, {} audio messages, {} documents, {} gifs, {} archives",
                    attachmentCount, photosCount, videosCount, audiosCount, audioMessagesCount, documentsCount, gifsCount, archivesCount
            );

            var messageSource = new SimpleMessageSource(messages);
            var exportOptions = new ExportOptions(conversation, userPreferences.getExportTypes(), messageSource, totalSizeLimit);
            var exportContext = new ExportContext(principal, exportOptions, attachmentDownloadService);

            final int progressInterval = 10;
            var progressStages = IntStream.iterate(progressInterval, n -> n <= 100, n -> n + progressInterval)
                    .boxed()
                    .collect(toSet());
            var resultObject = conversationExportService.export(exportContext, jobProgress -> {
                var totalItems = jobProgress.getTotal();
                var currentItem = jobProgress.getStep();
                var remainingSizeLimit = jobProgress.getRemainingSizeLimit();

                if ((totalItems == 0) || (remainingSizeLimit != null && remainingSizeLimit.compareTo(BigDecimal.ZERO) <= 0)) {
                    return;
                }

                var progress = (int) (currentItem * 100.0 / totalItems);
                progress -= (progress % progressInterval);
                if (progress != 0 && progressStages.contains(progress)) {
                    String appendix = "";
                    if (remainingSizeLimit != null) {
                        appendix = " Remaining export memory: " + convertBytesToMegabytes(remainingSizeLimit) + "Mb";
                    }

                    LOGGER.warn("{}%{}", progress, appendix);
                    progressStages.remove(progress);
                }
            });
            for (var downloadResult : resultObject.getAttachmentDownloadResults()) {
                if (!downloadResult.isSuccessful()) {
                    failures.add(new ExportFailure(conversation.getId(), downloadResult.getAttachment()));
                } else if (totalSizeLimit != null) {
                    var result = (SuccessfulAttachmentDownloadResult) downloadResult;
                    var file = result.getFile();
                    if (file != null) {
                        totalSizeLimit = totalSizeLimit.subtract(new BigDecimal(file.length()));
                    }
                }
            }
            var item = new ExportHistoryItem(conversation.getId(), lastMessage.getId(), lastMessage.getDate().plusSeconds(1));
            history.put(conversation.getId(), item);
            LOGGER.warn("*********************************************************************************************");
        }
    }

    private void saveHistory(@NonNull String exportHistoryPath, Map<Integer, ExportHistoryItem> history, List<ExportFailure> failures) {
        var exportHistory = new ExportHistory();
        exportHistory.setItems(history.values());
        exportHistory.setFailures(failures);
        LOGGER.warn("Saving history file...");
        contentService.write(exportHistoryPath, exportHistory);
        LOGGER.warn("History file saved");
    }

    private List<Conversation> loadConversations(@NonNull Set<Integer> conversationIdSet) {
        var result = new ArrayList<Conversation>();
        var principal = applicationContext.getPrincipal();

        try {
            if (conversationIdSet.isEmpty()) {
                final int limit = 200;
                var conversationList = vkClientMiddleware.loadConversationList(principal, 0, 1).get();
                int total = conversationList.getTotal();
                for (int offset = 0; offset < total; offset += limit) {
                    var list = vkClientMiddleware.loadConversationList(principal, offset, limit)
                            .thenApply(ConversationList::getItems)
                            .get();
                    result.addAll(list);
                }
            } else {
                var idSet = new HashSet<Integer>();
                for (Integer conversationId : conversationIdSet) {
                    idSet.add(conversationId);
                    if (idSet.size() >= 99) {
                        result.addAll(vkClientMiddleware.loadConversations(principal, idSet).get());
                        idSet.clear();
                    }
                }
                if (!idSet.isEmpty()) {
                    result.addAll(vkClientMiddleware.loadConversations(principal, idSet).get());
                    idSet.clear();
                }
            }

            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return result;
        }
    }

    private boolean isSizeLimitExceed(@NonNull BigDecimal value, BigDecimal limit) {
        return limit != null && value.compareTo(limit) >= 0;
    }

    private BigDecimal convertBytesToMegabytes(@NonNull BigDecimal bytesCount) {
        var n = new BigDecimal(1024);
        return bytesCount.divide(n.multiply(n), 3, RoundingMode.UP);
    }
}
