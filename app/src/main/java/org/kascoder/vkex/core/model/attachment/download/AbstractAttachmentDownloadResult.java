package org.kascoder.vkex.core.model.attachment.download;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kascoder.vkex.core.model.attachment.Attachment;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractAttachmentDownloadResult implements AttachmentDownloadResult {
    private final Attachment attachment;
}
