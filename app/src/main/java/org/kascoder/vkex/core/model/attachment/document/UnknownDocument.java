package org.kascoder.vkex.core.model.attachment.document;

import org.kascoder.vkex.core.model.attachment.AttachmentType;
import org.kascoder.vkex.core.model.attachment.Document;

public class UnknownDocument extends Document {
    public UnknownDocument() {
        super(AttachmentType.DOCUMENT_UNKNOWN);
    }
}
