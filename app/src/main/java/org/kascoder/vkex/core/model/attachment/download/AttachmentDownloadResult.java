package org.kascoder.vkex.core.model.attachment.download;

import org.kascoder.vkex.core.model.attachment.Attachment;

public interface AttachmentDownloadResult {
    boolean isSuccessful();
    Attachment getAttachment();
}
