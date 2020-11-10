package org.kascoder.vkex.core.model.options;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SearchOptions {
    @NonNull
    private Integer conversationId;
    private int startMessageId;
    private LocalDateTime from;
    private LocalDateTime to;
}
