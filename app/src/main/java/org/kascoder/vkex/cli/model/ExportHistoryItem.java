package org.kascoder.vkex.cli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExportHistoryItem {
    private final int conversationId;
    private final int lastExportedMessageId;
    private final LocalDateTime lastExportedMessageDateTime;

    @JsonCreator
    public ExportHistoryItem(@JsonProperty("conversationId") int conversationId,
                             @JsonProperty("lastExportedMessageId") int lastExportedMessageId,
                             @JsonProperty("lastExportedMessageDateTime") LocalDateTime lastExportedMessageDateTime) {
        this.conversationId = conversationId;
        this.lastExportedMessageId = lastExportedMessageId;
        this.lastExportedMessageDateTime = lastExportedMessageDateTime;
    }
}
