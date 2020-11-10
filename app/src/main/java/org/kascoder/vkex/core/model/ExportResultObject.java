package org.kascoder.vkex.core.model;

import lombok.NonNull;
import lombok.Value;
import org.kascoder.vkex.core.model.attachment.download.AttachmentDownloadResult;

import java.util.List;

@Value(staticConstructor = "of")
public class ExportResultObject {
    @NonNull
    List<AttachmentDownloadResult> attachmentDownloadResults;
}
