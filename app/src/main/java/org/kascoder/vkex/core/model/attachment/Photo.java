package org.kascoder.vkex.core.model.attachment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Photo extends Attachment {
    private String url;
    private Integer width;
    private Integer height;
    private String caption;
    private String accessKey;
    private Integer originalWidth;
    private Integer originalHeight;

    public Photo() {
        super(AttachmentType.PHOTO);
    }
}
