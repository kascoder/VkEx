package org.kascoder.vkex.core.util;

import io.kascoder.vkclient.domain.model.PhotoSize;
import org.kascoder.vkex.core.model.Message;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.kascoder.vkex.core.model.attachment.*;
import org.kascoder.vkex.core.model.attachment.document.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.kascoder.vkclient.domain.model.PhotoSize.Type.m;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.o;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.p;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.q;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.r;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.s;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.w;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.x;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.y;
import static io.kascoder.vkclient.domain.model.PhotoSize.Type.z;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class ModelConverter {
    public Message convert(@NonNull io.kascoder.vkclient.domain.model.Message msg) {
        Message message = new Message();
        message.setId(msg.getId());
        message.setSenderId(msg.getFrom());
        message.setContent(msg.getText());

        var date = LocalDateTime.ofInstant(Instant.ofEpochSecond(msg.getDateUnixTime()), ZoneId.systemDefault());
        message.setDate(date);

        List<io.kascoder.vkclient.domain.model.Attachment> attachments = new ArrayList<>();
        collectMessagesAttachments(List.of(msg), attachments);

        if (!attachments.isEmpty()) {
            var attachmentList = attachments.stream()
                    .map(ModelConverter::convert)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());
            message.setAttachmentList(attachmentList);
        }

        return message;
    }

    public Optional<Attachment> convert(io.kascoder.vkclient.domain.model.Attachment a) {
        if (a == null) {
            return Optional.empty();
        }

        var result = switch (a.getType()) {
            case VIDEO -> convert(a.getVideo());
            case AUDIO -> convert(a.getAudio());
            case AUDIO_MESSAGE -> convert(a.getAudioMessage());
            case PHOTO -> convert(a.getPhoto());
            case DOC -> convert(a.getDocument());
            default -> Optional.empty();
        };

        return result.map(Attachment.class::cast);
    }

    public Optional<Document> convert(io.kascoder.vkclient.domain.model.Document d) {
        if (d == null) {
            return Optional.empty();
        }

        var document = switch (d.getType()) {
            case TEXT -> new TextDocument();
            case ARCHIVE -> new ArchiveDocument();
            case GIF -> new GifDocument();
            case IMAGE -> new ImageDocument();
            case AUDIO -> new AudioDocument();
            case VIDEO -> new VideoDocument();
            case EBOOK -> new EbookDocument();
            case UNKNOWN -> new UnknownDocument();
        };

        document.setAccessKey(d.getAccessKey());
        document.setExtension(d.getExtension());
        document.setTitle(d.getTitle());
        document.setUrl(d.getUrl());

        return Optional.of(document);
    }

    public Optional<Photo> convert(io.kascoder.vkclient.domain.model.Photo p) {
        if (p.getSizeList() == null) {
            return Optional.empty();
        }

        return p.getSizeList()
                .stream()
                .max((ps1, ps2) -> comparePhotoSizeTypes(ps1.getType(), ps2.getType()))
                .map(photoSize -> {
                    Photo photo = new Photo();
                    photo.setAccessKey(p.getAccessKey());
                    photo.setCaption(p.getCaption());
                    photo.setOriginalWidth(p.getOriginalWidth());
                    photo.setOriginalHeight(p.getOriginalHeight());
                    photo.setUrl(photoSize.getUrl());
                    photo.setHeight(photoSize.getHeight());
                    photo.setWidth(photoSize.getWidth());

                    return photo;
                });

    }

    public Optional<AudioMessage> convert(io.kascoder.vkclient.domain.model.AudioMessage a) {
        if (a == null) {
            return Optional.empty();
        }

        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setAccessKey(a.getAccessKey());
        audioMessage.setMp3Url(a.getMp3Url());
        audioMessage.setOggUrl(a.getOggUrl());

        return Optional.of(audioMessage);
    }

    public Optional<Audio> convert(io.kascoder.vkclient.domain.model.Audio a) {
        if (a == null) {
            return Optional.empty();
        }

        Audio audio = new Audio();
        audio.setArtist(a.getArtist());
        audio.setUrl(a.getMp3Url());
        audio.setTitle(a.getTitle());

        return Optional.of(audio);
    }

    public Optional<Video> convert(io.kascoder.vkclient.domain.model.Video v) {
        if (v == null) {
            return Optional.empty();
        }

        Video video = new Video();
        video.setId(v.getId());
        video.setDate(v.getDate());
        video.setTitle(v.getTitle());
        video.setWidth(v.getWidth());
        video.setHeight(v.getHeight());
        video.setLive(v.isLive());
        video.setOwnerId(v.getOwnerId());
        video.setPlatform(v.getPlatform());
        video.setAccessKey(v.getAccessKey());
        video.setPlayerUrl(v.getPlayerUrl());
        video.setDescription(v.getDescription());
        video.setProcessing(v.isProcessing());
        var videoFile = v.getFile();
        if (videoFile != null) {
            if (StringUtils.isNotBlank(videoFile.getExternalUrl())) {
                video.setUrl(videoFile.getExternalUrl());
            } else if (StringUtils.isNotBlank(videoFile.getMp1080Url())) {
                video.setUrl(videoFile.getMp1080Url());
            } else if (StringUtils.isNotBlank(videoFile.getMp720Url())) {
                video.setUrl(videoFile.getMp720Url());
            } else if (StringUtils.isNotBlank(videoFile.getMp480Url())) {
                video.setUrl(videoFile.getMp480Url());
            } else if (StringUtils.isNotBlank(videoFile.getMp360Url())) {
                video.setUrl(videoFile.getMp360Url());
            } else if (StringUtils.isNotBlank(videoFile.getMp240Url())) {
                video.setUrl(videoFile.getMp240Url());
            }
        }

        return Optional.of(video);
    }

    private int comparePhotoSizeTypes(PhotoSize.Type type1, PhotoSize.Type type2) {
        var type2w = w.equals(type2);
        var type2z = z.equals(type2);
        var type2y = y.equals(type2);
        var type2r = r.equals(type2);
        var type2q = q.equals(type2);
        var type2p = p.equals(type2);
        var type2o = o.equals(type2);
        var type2x = x.equals(type2);
        var type2m = m.equals(type2);

        if (w.equals(type1)) {
            return 1;
        } else if (z.equals(type1) && !type2w) {
            return 1;
        } else if (y.equals(type1) && !type2w && !type2z) {
            return 1;
        } else if (r.equals(type1) && !type2w && !type2z && !type2y) {
            return 1;
        } else if (q.equals(type1) && !type2w && !type2z && !type2y && !type2r) {
            return 1;
        } else if (p.equals(type1) && !type2w && !type2z && !type2y && !type2r && !type2q) {
            return 1;
        } else if (o.equals(type1) && !type2w && !type2z && !type2y && !type2r && !type2q && !type2p) {
            return 1;
        } else if (x.equals(type1) && !type2w && !type2z && !type2y && !type2r && !type2q && !type2p && !type2o) {
            return 1;
        } else if (m.equals(type1) && !type2w && !type2z && !type2y && !type2r && !type2q && !type2p && !type2o && !type2x) {
            return 1;
        } else if (s.equals(type1) && !type2w && !type2z && !type2y && !type2r && !type2q && !type2p && !type2o && !type2x && !type2m) {
            return 1;
        }

        return -1;
    }

    private void collectMessagesAttachments(List<io.kascoder.vkclient.domain.model.Message> messages,
                                            List<io.kascoder.vkclient.domain.model.Attachment> attachmentStorage) {
        if (messages == null) {
            return;
        }

        for (io.kascoder.vkclient.domain.model.Message message : messages) {
            if (message.getAttachments() != null) {
                attachmentStorage.addAll(message.getAttachments());
            }

            collectMessagesAttachments(message.getForwardedMessages(), attachmentStorage);
        }
    }
}
