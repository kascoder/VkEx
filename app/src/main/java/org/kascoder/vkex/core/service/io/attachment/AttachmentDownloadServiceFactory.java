package org.kascoder.vkex.core.service.io.attachment;

import org.kascoder.vkex.core.model.options.StorageOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AttachmentDownloadServiceFactory {
    public static AttachmentDownloadService build(@NonNull StorageOptions storageOptions) {
        return switch (storageOptions.getType()) {
            case LOCAL -> new LocalStorageAttachmentDownloadService(storageOptions.getPath());
        };
    }
}
