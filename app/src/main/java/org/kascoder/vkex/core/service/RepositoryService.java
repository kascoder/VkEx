package org.kascoder.vkex.core.service;

import org.kascoder.vkex.core.model.Update;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RepositoryService {
    CompletableFuture<Optional<Update>> checkForUpdatesAsync();

    default Optional<Update> checkForUpdates() {
        try {
            return checkForUpdatesAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
