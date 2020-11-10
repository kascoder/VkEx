package org.kascoder.vkex.core.model.attachment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class Document extends Attachment {
    private String title;
    private String url;
    private String accessKey;
    private String extension;

    protected Document(AttachmentType type) {
        super(type);
    }
}
