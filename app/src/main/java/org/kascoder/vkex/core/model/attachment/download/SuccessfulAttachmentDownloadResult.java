package org.kascoder.vkex.core.model.attachment.download;

import org.kascoder.vkex.core.model.attachment.Attachment;
import lombok.Getter;

import java.io.File;

@Getter
public class SuccessfulAttachmentDownloadResult extends AbstractAttachmentDownloadResult {
    private final File file;

    public SuccessfulAttachmentDownloadResult(Attachment attachment, File file) {
        super(attachment);
        this.file = file;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }
}
