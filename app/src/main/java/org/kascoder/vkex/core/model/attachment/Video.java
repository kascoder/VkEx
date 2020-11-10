package org.kascoder.vkex.core.model.attachment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Video extends Attachment {
    private Integer id;
    private String url;
    private Integer date;
    private String title;
    private Integer width;
    private Integer height;
    private boolean isLive;
    private Integer ownerId;
    private String platform;
    private String accessKey;
    private String playerUrl;
    private String description;
    private boolean isProcessing;

    public Video() {
        super(AttachmentType.VIDEO);
    }
}
