package org.kascoder.vkex.core.model.attachment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Audio extends Attachment {
    private String title;
    private String url;
    private String artist;

    public Audio() {
        super(AttachmentType.AUDIO);
    }
}
