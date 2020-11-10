package org.kascoder.vkex.core.model.attachment.document;

import org.kascoder.vkex.core.model.attachment.AttachmentType;
import org.kascoder.vkex.core.model.attachment.Document;

public class TextDocument extends Document {
    public TextDocument() {
        super(AttachmentType.DOCUMENT_TEXT);
    }
}
