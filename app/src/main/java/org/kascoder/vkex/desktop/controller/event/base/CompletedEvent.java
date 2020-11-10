package org.kascoder.vkex.desktop.controller.event.base;

public interface CompletedEvent extends ProgressEvent {
    @Override
    default boolean isRunning() {
        return false;
    }
}
