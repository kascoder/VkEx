package org.kascoder.vkex.core.model.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.kascoder.vkex.core.util.AudioMessageDeserializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@JsonDeserialize(using = AudioMessageDeserializer.class)
public class AudioMessage extends Attachment {
    private String accessKey;
    private String mp3Url;
    private String oggUrl;

    public AudioMessage() {
        super(AttachmentType.AUDIO_MESSAGE);
    }

    @Override
    @JsonIgnore
    public String getUrl() {
        return mp3Url == null || mp3Url.isEmpty() ? oggUrl : mp3Url;
    }
}
