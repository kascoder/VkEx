package org.kascoder.vkex.core.service.io;

import lombok.NonNull;

import java.util.Optional;

public interface ContentService {
    void write(@NonNull String path, @NonNull Object obj);

    <T> Optional<T> read(@NonNull String path, @NonNull Class<T> clazz);

    boolean exist(@NonNull String path);

    String buildFullFilePath(@NonNull String partialFilePath);
}
