package org.kascoder.vkex.desktop.controller.event;

import org.kascoder.vkex.desktop.controller.event.base.CompletedEvent;
import org.kascoder.vkex.desktop.controller.event.base.SearchEvent;
import lombok.NonNull;
import lombok.Value;
import org.kascoder.vkex.core.model.Message;

import java.util.List;

@Value
public class SearchCompletedEvent implements SearchEvent, CompletedEvent {
    @NonNull List<Message> messages;
}
