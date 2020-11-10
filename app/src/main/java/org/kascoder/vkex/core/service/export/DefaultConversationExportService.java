package org.kascoder.vkex.core.service.export;

import org.kascoder.vkex.core.model.ExportResultObject;
import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.JobProgress;
import org.kascoder.vkex.core.model.attachment.Attachment;
import org.kascoder.vkex.core.model.attachment.download.AttachmentDownloadResult;
import org.kascoder.vkex.core.model.attachment.download.SuccessfulAttachmentDownloadResult;
import org.kascoder.vkex.core.model.attachment.download.UnsuccessfulAttachmentDownloadResult;
import org.kascoder.vkex.core.util.ExportContext;
import org.kascoder.vkex.core.util.JobProgressNotifier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultConversationExportService implements ConversationExportService {
    @Override
    public ExportResultObject export(@NonNull ExportContext exportContext,
                                     @NonNull JobProgressNotifier exportProgressNotifier) {
        int exportStep = 0;
        List<AttachmentDownloadResult> results = new ArrayList<>();

        @NonNull var exportOptions = exportContext.getExportOptions();
        @NonNull var attachmentDownloadService = exportContext.getAttachmentDownloadService();

        @NonNull var exportTypes = exportOptions.getExportTypes();
        @NonNull var messageSource = exportOptions.getMessageSource();
        var totalSizeLimit = exportOptions.getTotalSizeLimit();

        attachmentDownloadService.prepareEnvironment();
        while (true) {
            var messageValue = messageSource.next();

            var caption = generateCaption(exportStep, messageSource.getAttachmentCount(), "Attachments");
            var jobProgress = new JobProgress(exportStep, messageSource.getAttachmentCount(), caption, totalSizeLimit);
            exportProgressNotifier.notify(jobProgress);

            if (messageValue.isEmpty()) {
                break;
            }

            var message = messageValue.get();
            if (message.hasAttachments()) {
                for (Attachment attachment : message.getAttachmentList()) {
                    if (!exportTypes.contains(ExportType.from(attachment))) {
                        continue;
                    }

                    if (totalSizeLimit != null && totalSizeLimit.compareTo(BigDecimal.ZERO) <= 0) {
                        results.add(new UnsuccessfulAttachmentDownloadResult(attachment, new Exception("Size limit exceeded")));
                        continue;
                    }

                    var downloadResult = attachmentDownloadService.downloadAttachment(attachment);
                    if (downloadResult.isSuccessful() && totalSizeLimit != null) {
                        var result = (SuccessfulAttachmentDownloadResult) downloadResult;
                        var file = result.getFile();
                        if (file != null) {
                            totalSizeLimit = totalSizeLimit.subtract(new BigDecimal(file.length()));
                        }
                    }

                    results.add(downloadResult);
                }

                exportStep += message.attachmentCount();
            }
        }
        attachmentDownloadService.cleanUpEnvironment();

        return ExportResultObject.of(results);
    }

    private String generateCaption(Integer step, Integer total, String stepEntity) {
        return String.format("%d / %d %s", step, total, stepEntity);
    }
}
