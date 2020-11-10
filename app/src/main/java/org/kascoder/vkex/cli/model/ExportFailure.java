package org.kascoder.vkex.cli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.kascoder.vkex.core.model.attachment.Attachment;
import lombok.Getter;

@Getter
public class ExportFailure {
    private final int conversationId;
    private final Attachment attachment;

    @JsonCreator
    public ExportFailure(@JsonProperty("conversationId") int conversationId,
                         @JsonProperty("attachment") Attachment attachment) {
        this.conversationId = conversationId;
        this.attachment = attachment;
    }
}
