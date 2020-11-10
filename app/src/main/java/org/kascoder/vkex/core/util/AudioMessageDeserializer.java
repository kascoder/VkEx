package org.kascoder.vkex.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.kascoder.vkex.core.model.attachment.AudioMessage;

import java.io.IOException;

public class AudioMessageDeserializer extends StdDeserializer<AudioMessage> {
    public AudioMessageDeserializer() {
        this(null);
    }

    protected AudioMessageDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public AudioMessage deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String accessKey = node.get("accessKey").asText();
        String mp3Url = node.get("mp3Url").asText();
        String oggUrl = node.get("oggUrl").asText();

        var audioMessage = new AudioMessage();
        audioMessage.setAccessKey(accessKey);
        audioMessage.setMp3Url(mp3Url);
        audioMessage.setOggUrl(oggUrl);

        return audioMessage;
    }
}
