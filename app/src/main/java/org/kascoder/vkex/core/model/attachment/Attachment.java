package org.kascoder.vkex.core.model.attachment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.kascoder.vkex.core.model.attachment.document.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @Type(value = Audio.class, name = "AUDIO"),
        @Type(value = AudioMessage.class, name = "AUDIO_MESSAGE"),
        @Type(value = Video.class, name = "VIDEO"),
        @Type(value = Photo.class, name = "PHOTO"),
        @Type(value = TextDocument.class, name = "DOCUMENT_TEXT"),
        @Type(value = ArchiveDocument.class, name = "DOCUMENT_ARCHIVE"),
        @Type(value = GifDocument.class, name = "DOCUMENT_GIF"),
        @Type(value = ImageDocument.class, name = "DOCUMENT_IMAGE"),
        @Type(value = AudioDocument.class, name = "DOCUMENT_AUDIO"),
        @Type(value = EbookDocument.class, name = "DOCUMENT_EBOOK"),
        @Type(value = VideoDocument.class, name = "DOCUMENT_VIDEO"),
        @Type(value = UnknownDocument.class, name = "DOCUMENT_UNKNOWN")
})
public abstract class Attachment {
    @NonNull
    private final AttachmentType type;

    public abstract String getUrl();
}
