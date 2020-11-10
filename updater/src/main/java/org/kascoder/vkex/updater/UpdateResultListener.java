package org.kascoder.vkex.updater;

@FunctionalInterface
public interface UpdateResultListener {
    void completed(boolean success, Exception e);
}
