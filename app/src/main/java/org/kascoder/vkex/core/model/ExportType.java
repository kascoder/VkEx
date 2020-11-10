package org.kascoder.vkex.core.model;

import org.kascoder.vkex.core.model.attachment.Attachment;
import org.kascoder.vkex.core.model.attachment.AttachmentType;
import lombok.NonNull;

public enum ExportType {
    ARCHIVE,
    AUDIO,
    AUDIO_MESSAGE,
    DOCUMENT,
    PHOTO,
    PHOTO_GIF,
    VIDEO;

    public static ExportType from(@NonNull Attachment attachment) {
        return from(attachment.getType());
    }

    public static ExportType from(@NonNull AttachmentType attachmentType) {
        return switch (attachmentType) {
            case DOCUMENT_ARCHIVE -> ExportType.ARCHIVE;
            case AUDIO, DOCUMENT_AUDIO -> ExportType.AUDIO;
            case AUDIO_MESSAGE -> ExportType.AUDIO_MESSAGE;
            case PHOTO, PHOTO_M, PHOTO_O, PHOTO_P, PHOTO_Q, PHOTO_R, PHOTO_S, PHOTO_W, PHOTO_X, PHOTO_Y, PHOTO_Z, DOCUMENT_IMAGE -> ExportType.PHOTO;
            case VIDEO, DOCUMENT_VIDEO -> ExportType.VIDEO;
            case DOCUMENT_GIF -> ExportType.PHOTO_GIF;
            case DOCUMENT_TEXT, DOCUMENT_EBOOK, DOCUMENT_UNKNOWN -> ExportType.DOCUMENT;
        };
    }
}
