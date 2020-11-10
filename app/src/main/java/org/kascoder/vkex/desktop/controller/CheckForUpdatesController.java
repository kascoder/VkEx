package org.kascoder.vkex.desktop.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import org.kascoder.vkex.core.model.Update;
import org.kascoder.vkex.core.service.RepositoryService;
import org.kascoder.vkex.core.service.UpdateService;
import org.kascoder.vkex.desktop.util.UiUtils;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CheckForUpdatesController implements ApplicationController {
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Text infoText;
    @FXML
    private Hyperlink appWebsiteLink;
    @FXML
    private Button updateButton;

    private final UpdateService updateService;
    private final RepositoryService repositoryService;

    @Inject
    public CheckForUpdatesController(UpdateService updateService, RepositoryService repositoryService) {
        this.updateService = updateService;
        this.repositoryService = repositoryService;
    }

    @Override
    public void initialize() {
        repositoryService.checkForUpdatesAsync()
                .whenComplete(this::processCheckForUpdatesResult);
        appWebsiteLink.setOnAction(event -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URL("https://vk.com/public195065105").toURI());
                } catch (IOException | URISyntaxException e) {
                    UiUtils.notifyError(e);
                }
            }
        });
    }

    private void processCheckForUpdatesResult(Optional<Update> update, Throwable error) {
        Platform.runLater(() -> {
            infoText.setVisible(true);
            progressIndicator.setVisible(false);
            if (error != null) {
                infoText.setText(error.getMessage());
                return;
            }

            update.ifPresentOrElse(upd -> {
                if (upd.isBackwardCompatible()) {
                    infoText.setText("There's a new version of VkEx");
                    updateButton.setVisible(true);
                    updateButton.setOnAction(event -> startUpdate(upd));
                } else {
                    infoText.setText("The new version of VkEx isn't backward compatible. You have to re-install VkEx using the link below");
                    appWebsiteLink.setVisible(true);
                }
            }, () -> infoText.setText("Application is up to date"));
        });
    }

    private void startUpdate(Update upd) {
        updateButton.setDisable(true);
        progressIndicator.setVisible(true);
        CompletableFuture.runAsync(() -> updateService.initiateUpdate(upd, true, this::handleUpdateError));
    }

    private void handleUpdateError(Exception throwable) {
        Platform.runLater(() -> {
            updateButton.setDisable(false);
            progressIndicator.setVisible(false);
            UiUtils.notifyError(throwable);
        });
    }
}
