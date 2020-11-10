package org.kascoder.vkex.core.model.attachment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentType {
    AUDIO("Audio"),
    AUDIO_MESSAGE("Audio Message"),
    VIDEO("Video"),
    PHOTO("Photo"), PHOTO_S("Photo"), PHOTO_M("Photo"), PHOTO_X("Photo"), PHOTO_O("Photo"), PHOTO_P("Photo"), PHOTO_Q("Photo"), PHOTO_R("Photo"), PHOTO_Y("Photo"), PHOTO_Z("Photo"), PHOTO_W("Photo"),
    DOCUMENT_TEXT("Text Document"), DOCUMENT_ARCHIVE("Archive Document"), DOCUMENT_GIF("Gif Document"), DOCUMENT_IMAGE("Image Document"), DOCUMENT_AUDIO("Audio Document"), DOCUMENT_VIDEO("Video Document"), DOCUMENT_EBOOK("Ebook Document"), DOCUMENT_UNKNOWN("Unknown Document");

    private final String caption;
}
