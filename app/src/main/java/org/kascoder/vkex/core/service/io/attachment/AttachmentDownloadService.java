package org.kascoder.vkex.core.service.io.attachment;

import lombok.NonNull;
import org.kascoder.vkex.core.model.attachment.Attachment;
import org.kascoder.vkex.core.model.attachment.download.AttachmentDownloadResult;

public interface AttachmentDownloadService {

    String AUDIOS_DOWNLOAD_FOLDER_NAME = "audios";
    String AUDIO_MESSAGES_DOWNLOAD_FOLDER_NAME = "audio_messages";
    String VIDEOS_DOWNLOAD_FOLDER_NAME = "videos";
    String PHOTOS_DOWNLOAD_FOLDER_NAME = "photos";
    String DOCUMENTS_DOWNLOAD_FOLDER_NAME = "documents";
    String ARCHIVES_DOWNLOAD_FOLDER_NAME = "archives";

    AttachmentDownloadResult downloadAttachment(@NonNull Attachment attachment);

    void prepareEnvironment();

    void cleanUpEnvironment();
}
