package org.kascoder.vkex.core.util;

import org.kascoder.vkex.core.model.Principal;
import org.kascoder.vkex.core.model.options.ExportOptions;
import org.kascoder.vkex.core.service.io.attachment.AttachmentDownloadService;
import lombok.NonNull;
import lombok.Value;

@Value
public class ExportContext {
    @NonNull Principal principal;
    @NonNull ExportOptions exportOptions;
    @NonNull AttachmentDownloadService attachmentDownloadService;
}
