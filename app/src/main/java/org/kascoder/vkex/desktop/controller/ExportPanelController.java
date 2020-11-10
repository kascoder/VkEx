package org.kascoder.vkex.desktop.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.scene.control.*;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.JobProgress;
import org.kascoder.vkex.core.model.Message;
import org.kascoder.vkex.core.model.source.MessageSource;
import org.kascoder.vkex.core.model.source.SimpleMessageSource;
import org.kascoder.vkex.desktop.controller.event.ConversationSelectionResetEvent;
import org.kascoder.vkex.desktop.controller.event.ExportRequestedEvent;
import org.kascoder.vkex.desktop.controller.event.ExportStoppedEvent;
import org.kascoder.vkex.desktop.controller.event.SearchCompletedEvent;
import org.kascoder.vkex.desktop.controller.event.base.ExportEvent;
import org.kascoder.vkex.desktop.util.FXSceneRouter;
import org.kascoder.vkex.desktop.util.UiUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import lombok.NonNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExportPanelController implements ApplicationController {

    private final String DEFAULT_DOWNLOAD_PATH;
    private final boolean exportAudios;
    private final boolean exportAudioMessages;
    private final boolean exportDocuments;
    private final boolean exportArchives;
    private final boolean exportGifs;
    private final boolean exportPhotos;
    private final boolean exportVideos;

    @FXML private VBox panel;
    @FXML private HBox optionsBar;
    @FXML private HBox pathBar;
    @FXML private CheckBox audioCheck;
    @FXML private CheckBox audioMsgCheck;
    @FXML private CheckBox documentCheck;
    @FXML private CheckBox photoCheck;
    @FXML private CheckBox videoCheck;
    @FXML private CheckBox archiveCheck;
    @FXML private CheckBox gifCheck;
    @FXML private Hyperlink downloadPathLink;

    @FXML private Button exportBtn;
    @FXML private Button stopExportBtn;
    @FXML private Pane exportProgressPane;
    @FXML private ProgressBar messageExportProgress;
    @FXML private Label messageExportProgressCaption;

    private final EventBus eventBus;
    private final List<Message> history;

    @Inject
    public ExportPanelController(EventBus eventBus, ApplicationContext applicationContext) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.history = new ArrayList<>();

        var userPreferences = applicationContext.getUserPreferences();
        @NonNull var storageOptions = userPreferences.getStorageOptions();
        @NonNull var exportTypes = userPreferences.getExportTypes();
        this.DEFAULT_DOWNLOAD_PATH = storageOptions.getPath();
        this.exportAudios = exportTypes.contains(ExportType.AUDIO);
        this.exportAudioMessages = exportTypes.contains(ExportType.AUDIO_MESSAGE);
        this.exportDocuments = exportTypes.contains(ExportType.DOCUMENT);
        this.exportPhotos = exportTypes.contains(ExportType.PHOTO);
        this.exportVideos = exportTypes.contains(ExportType.VIDEO);
        this.exportArchives = exportTypes.contains(ExportType.ARCHIVE);
        this.exportGifs = exportTypes.contains(ExportType.PHOTO_GIF);
    }

    @Override
    public void initialize() {
        setDownloadPath(DEFAULT_DOWNLOAD_PATH);
        selectUserPreferredExportTypes();
    }

    public void chooseDownloadDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select download folder");
        directoryChooser.setInitialDirectory(new File(DEFAULT_DOWNLOAD_PATH));

        File userSelectionDir = directoryChooser.showDialog(FXSceneRouter.currentStage());
        setDownloadPath(userSelectionDir == null ? DEFAULT_DOWNLOAD_PATH : userSelectionDir.getPath());
        downloadPathLink.setVisited(false);
    }

    public void initiateExportStart() {
        MessageSource messageSource;
        try {
            messageSource = buildMessageSource();
        } catch (Exception e) {
            UiUtils.notifyError(e.getMessage());
            return;
        }
        var downloadPath = getDownloadPathForExecution();
        eventBus.post(new ExportRequestedEvent(true, downloadPath, messageSource, buildExportTypes(), this::notifyExportProgress));
    }

    public void initiateExportStop() {
        var question = "Are you sure you want to stop exporting data?\n\nIf you do, you'll need to start over.";
        UiUtils.showConfirmation(question, ButtonType.YES, ButtonType.NO, btnType -> {
            if (ButtonType.YES.equals(btnType)) {
                eventBus.post(new ExportStoppedEvent());
            }
        });
    }

    private MessageSource buildMessageSource() throws Exception {
        if (this.history.isEmpty()) {
            throw new Exception("Nothing to export.");
        }
        return new SimpleMessageSource(this.history);
    }

    private Set<ExportType> buildExportTypes() {
        var result = new HashSet<ExportType>();
        if (audioCheck.isSelected()) {
            result.add(ExportType.AUDIO);
        }

        if (audioMsgCheck.isSelected()) {
            result.add(ExportType.AUDIO_MESSAGE);
        }

        if (videoCheck.isSelected()) {
            result.add(ExportType.VIDEO);
        }

        if (photoCheck.isSelected()) {
            result.add(ExportType.PHOTO);
        }

        if (gifCheck.isSelected()) {
            result.add(ExportType.PHOTO_GIF);
        }

        if (documentCheck.isSelected()) {
            result.add(ExportType.DOCUMENT);
            result.add(ExportType.ARCHIVE);
        }

        if (archiveCheck.isSelected()) {
            result.add(ExportType.ARCHIVE);
        }

        return result;
    }

    private String getDownloadPathForExecution() {
        return Paths.get(downloadPathLink.getText()).toString();
    }

    private void setDownloadPath(String path) {
        String downloadPath = path;
        int limit = 45;
        if (downloadPath.length() > limit) {
            downloadPath = downloadPath.substring(0, limit - 3) + "...";
        }

        downloadPathLink.setText(downloadPath);
    }

    private void notifyExportProgress(@NonNull JobProgress job) {
        Platform.runLater(() -> {
            messageExportProgress.setProgress(job.getStep() * 1.0 / job.getTotal());
            messageExportProgressCaption.setText(job.getCaption());
        });
    }

    @Subscribe
    private void handleSearchCompletedEvent(SearchCompletedEvent event) {
        var messages = event.getMessages();

        if (messages.size() > 0) {
            panel.setVisible(true);
        }

        this.history.clear();
        this.history.addAll(messages);
    }

    @Subscribe
    private void resetOptions(ConversationSelectionResetEvent event) {
        selectUserPreferredExportTypes();
        this.history.clear();
        this.stopExportBtn.setDisable(true);
        this.exportBtn.setDisable(false);
        this.exportProgressPane.setVisible(false);
        this.messageExportProgress.setProgress(0);

        this.panel.setVisible(false);

        setDownloadPath(DEFAULT_DOWNLOAD_PATH);
    }

    private void selectUserPreferredExportTypes() {
        this.audioCheck.setSelected(this.exportAudios);
        this.audioMsgCheck.setSelected(this.exportAudioMessages);
        this.documentCheck.setSelected(this.exportDocuments);
        this.photoCheck.setSelected(this.exportPhotos);
        this.videoCheck.setSelected(this.exportVideos);
        this.archiveCheck.setSelected(this.exportArchives);
        this.gifCheck.setSelected(this.exportGifs);
    }

    @Subscribe
    private void handleExportEvent(ExportEvent event) {
        var exportInProgress = event.isRunning();

        this.optionsBar.setDisable(exportInProgress);
        this.pathBar.setDisable(exportInProgress);
        this.exportBtn.setDisable(exportInProgress);
        this.stopExportBtn.setDisable(!exportInProgress);
        this.exportProgressPane.setVisible(exportInProgress);
        this.messageExportProgress.setProgress(0);
    }
}
