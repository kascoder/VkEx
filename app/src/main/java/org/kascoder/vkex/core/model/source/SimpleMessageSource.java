package org.kascoder.vkex.core.model.source;

import lombok.Getter;
import lombok.NonNull;
import org.kascoder.vkex.core.model.Message;
import org.kascoder.vkex.core.model.util.MessageSourceClosedException;

import java.util.*;

public class SimpleMessageSource implements MessageSource {
    private boolean closed;
    @Getter
    private final int messageCount;
    @Getter
    private final int attachmentCount;
    private final Queue<Message> messages;

    public SimpleMessageSource(@NonNull List<Message> messages) {
        this.messages = new LinkedList<>(messages);
        this.messageCount = this.messages.size();
        this.attachmentCount = this.messages.stream()
                .map(Message::getAttachmentList)
                .mapToInt(Collection::size)
                .sum();
    }

    @Override
    public Optional<Message> next() {
        if (closed) {
            throw new MessageSourceClosedException();
        }
        return Optional.ofNullable(messages.poll());
    }

    @Override
    public void close() {
        this.closed = true;
    }
}
