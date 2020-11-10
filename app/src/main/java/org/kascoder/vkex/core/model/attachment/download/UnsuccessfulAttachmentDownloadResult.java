package org.kascoder.vkex.core.model.attachment.download;

import lombok.Getter;
import org.kascoder.vkex.core.model.attachment.Attachment;

@Getter
public class UnsuccessfulAttachmentDownloadResult extends AbstractAttachmentDownloadResult {
    private final Exception error;

    public UnsuccessfulAttachmentDownloadResult(Attachment attachment, Exception error) {
        super(attachment);
        this.error = error;
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }
}
