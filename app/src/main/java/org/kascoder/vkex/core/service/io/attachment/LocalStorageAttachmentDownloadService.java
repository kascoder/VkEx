package org.kascoder.vkex.core.service.io.attachment;

import org.kascoder.vkex.core.model.attachment.*;
import org.kascoder.vkex.core.model.attachment.download.AttachmentDownloadResult;
import org.kascoder.vkex.core.model.attachment.download.SuccessfulAttachmentDownloadResult;
import org.kascoder.vkex.core.model.attachment.download.UnsuccessfulAttachmentDownloadResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class LocalStorageAttachmentDownloadService implements AttachmentDownloadService {
    @NonNull
    private final String path;

    public AttachmentDownloadResult downloadAttachment(@NonNull Attachment attachment) {
        var downloadPath = path;
        try {
            var file = switch (attachment.getType()) {
                case AUDIO -> {
                    var audioFolderPath = getPath(downloadPath, AUDIOS_DOWNLOAD_FOLDER_NAME);
                    yield downloadAudio(audioFolderPath, (Audio) attachment);
                }
                case DOCUMENT_AUDIO -> {
                    var documentFolderPath = getPath(downloadPath, AUDIOS_DOWNLOAD_FOLDER_NAME);
                    yield downloadDocument(documentFolderPath, (Document) attachment);
                }
                case AUDIO_MESSAGE -> {
                    var audioMessageFolderPath = getPath(downloadPath, AUDIO_MESSAGES_DOWNLOAD_FOLDER_NAME);
                    yield downloadAudioMessage(audioMessageFolderPath, (AudioMessage) attachment);
                }
                case VIDEO -> {
                    var videoFolderPath = getPath(downloadPath, VIDEOS_DOWNLOAD_FOLDER_NAME);
                    yield downloadVideo(videoFolderPath, (Video) attachment);
                }
                case DOCUMENT_VIDEO -> {
                    var documentFolderPath = getPath(downloadPath, VIDEOS_DOWNLOAD_FOLDER_NAME);
                    yield downloadDocument(documentFolderPath, (Document) attachment);
                }
                case PHOTO -> {
                    var photoFolderPath = getPath(downloadPath, PHOTOS_DOWNLOAD_FOLDER_NAME);
                    yield downloadPhoto(photoFolderPath, (Photo) attachment);
                }
                case DOCUMENT_GIF, DOCUMENT_IMAGE -> {
                    var documentFolderPath = getPath(downloadPath, PHOTOS_DOWNLOAD_FOLDER_NAME);
                    yield downloadDocument(documentFolderPath, (Document) attachment);
                }
                case DOCUMENT_ARCHIVE -> {
                    var documentFolderPath = getPath(downloadPath, ARCHIVES_DOWNLOAD_FOLDER_NAME);
                    yield downloadDocument(documentFolderPath, (Document) attachment);
                }
                case DOCUMENT_TEXT, DOCUMENT_EBOOK, DOCUMENT_UNKNOWN -> {
                    var documentFolderPath = getPath(downloadPath, DOCUMENTS_DOWNLOAD_FOLDER_NAME);
                    yield downloadDocument(documentFolderPath, (Document) attachment);
                }
                default -> (File) null;
            };

            if (file != null) {
                LOGGER.info("Result=SUCCESS, attachment={}, path={}", attachment, file.getAbsolutePath());
            }

            return new SuccessfulAttachmentDownloadResult(attachment, file);
        } catch (Exception e) {
            LOGGER.error("Result=FAIL, cause: {}, attachment={}", e.getMessage(), attachment);
            return new UnsuccessfulAttachmentDownloadResult(attachment, e);
        }
    }

    @Override
    public void prepareEnvironment() {
        LOGGER.info("Preparing environment for attachments downloading...");
        var exportFolderPath = Path.of(path);
        if (Files.exists(exportFolderPath)) {
            if (!Files.isDirectory(exportFolderPath)) {
                throw new RuntimeException("Provided path doesn't direct to folder");
            }
            return;
        }

        if (!createDirectories(path)) {
            throw new RuntimeException("Export folder wasn't created");
        }

        LOGGER.info("Environment preparing completed");
    }

    @Override
    public void cleanUpEnvironment() {
        LOGGER.info("Environment cleanup completed");
    }

    private File downloadAudio(String path, Audio audio) throws Exception {
        var url = audio.getUrl();
        var audioUrlPath = new URL(url).getPath();
        String fileName;
        if (audio.getTitle() == null || audio.getArtist() == null) {
            fileName = FilenameUtils.getName(audioUrlPath);
        } else {
            fileName = audio.getArtist() + " - " + audio.getTitle();
            fileName += "." + FilenameUtils.getExtension(audioUrlPath);
        }

        if (!createDirectories(path)) {
            throw new RuntimeException("Audios folder wasn't created");
        }

        var filePath = Paths.get(path, fileName);

        return downloadFile(filePath, url);
    }

    private File downloadVideo(String path, Video video) throws Exception {
        if (StringUtils.isNotBlank(video.getPlatform())) {
            LOGGER.info("Video is from foreign platform. Skipped. Video: {}", video);
            return null;
        } else if (StringUtils.isBlank(video.getUrl())) {
            LOGGER.info("Video info wasn't fetched. Skipped. Video: {}", video);
            return null;
        }

        if (!createDirectories(path)) {
            throw new RuntimeException("Videos folder wasn't created");
        }

        var url = video.getUrl();
        var fileName = FilenameUtils.getName(new URL(url).getPath());
        var filePath = Paths.get(path, fileName);

        return downloadFile(filePath, url);
    }

    private File downloadAudioMessage(String path, AudioMessage audioMessage) throws Exception {
        if (!createDirectories(path)) {
            throw new RuntimeException("Audio messages folder wasn't created");
        }

        var mp3Url = audioMessage.getMp3Url();
        var fileName = FilenameUtils.getName(new URL(mp3Url).getPath());
        var filePath = Paths.get(path, fileName);

        return downloadFile(filePath, mp3Url);
    }

    private File downloadPhoto(String path, Photo photo) throws Exception {
        if (!createDirectories(path)) {
            throw new RuntimeException("Photos folder wasn't created");
        }

        var url = photo.getUrl();
        var fileName = FilenameUtils.getName(new URL(url).getPath());
        var filePath = Paths.get(path, fileName);

        return downloadFile(filePath, url);
    }

    private File downloadDocument(String path, Document document) throws Exception {
        if (!createDirectories(path)) {
            throw new RuntimeException("Documents folder wasn't created");
        }

        var fileName = document.getTitle();
        if (StringUtils.isNotBlank(document.getExtension())) {
            fileName = UUID.randomUUID().toString() + "." + document.getExtension();
        }

        var url = document.getUrl();
        var filePath = Paths.get(path, fileName);

        return downloadFile(filePath, url);
    }

    private File downloadFile(Path path, String url) throws Exception {
        var fileFullPath = path;
        if (Files.exists(path)) {
            fileFullPath = generateUniquePath(fileFullPath);
        }

        Files.createFile(fileFullPath);

        var destination = fileFullPath.toFile();
        FileUtils.copyURLToFile(new URL(url), destination);
        return destination;
    }

    private Path generateUniquePath(Path path) {
        var result = path;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            if (Files.notExists(result)) {
                break;
            }

            var name = FilenameUtils.getBaseName(path.toString());
            var extension = FilenameUtils.getExtension(path.toString());
            var fullPathNoEndSeparator = FilenameUtils.getFullPathNoEndSeparator(path.toString());
            var resultFileName = name + " (" + i + ")." + extension;
            result = Path.of(fullPathNoEndSeparator, resultFileName);
        }

        return result;
    }

    private boolean createDirectories(String path) {
        try {
            Files.createDirectories(Path.of(path));
            return true;
        } catch (IOException e) {
            LOGGER.info(e.getMessage(), e);
            return false;
        }
    }

    private String getPath(String first, String... more) {
        return Path.of(first, more).toString();
    }
}
