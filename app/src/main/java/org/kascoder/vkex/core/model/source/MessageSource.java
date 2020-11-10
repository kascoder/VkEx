package org.kascoder.vkex.core.model.source;

import org.kascoder.vkex.core.model.Message;

import java.util.Optional;

public interface MessageSource {

    Optional<Message> next();

    void close();

    int getMessageCount();

    int getAttachmentCount();
}
