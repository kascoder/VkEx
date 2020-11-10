package org.kascoder.vkex.desktop.controller.event;

import lombok.NonNull;
import lombok.Value;
import org.kascoder.vkex.core.model.Conversation;

@Value
public class ConversationOpenEvent {
    @NonNull Conversation conversation;
}
