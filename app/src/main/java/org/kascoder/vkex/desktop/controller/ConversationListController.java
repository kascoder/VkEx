package org.kascoder.vkex.desktop.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import io.kascoder.vkclient.Error;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.ApplicationConfiguration;
import org.kascoder.vkex.core.model.Conversation;
import org.kascoder.vkex.core.service.VkClientMiddleware;
import org.kascoder.vkex.core.service.settings.ConfigurationService;
import org.kascoder.vkex.desktop.controller.event.ConversationOpenEvent;
import org.kascoder.vkex.desktop.controller.event.base.ConversationListRefreshEvent;
import org.kascoder.vkex.desktop.controller.event.base.ProgressEvent;
import org.kascoder.vkex.desktop.util.FXSceneRouter;
import org.kascoder.vkex.desktop.util.Scene;
import org.kascoder.vkex.desktop.util.UiUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ConversationListController implements ApplicationController {
    @FXML private VBox conversationListHolder;
    @FXML private Button refreshBtn;
    @FXML private MenuButton menuButton;
    @FXML private ListView<Conversation> conversations;

    private final EventBus eventBus;
    private final ApplicationContext applicationContext;
    private final VkClientMiddleware vkClientMiddleware;
    private final ConfigurationService configurationService;

    @Inject
    public ConversationListController(EventBus eventBus,
                                      ApplicationContext applicationContext,
                                      VkClientMiddleware vkClientMiddleware,
                                      ConfigurationService configurationService) {
        this.eventBus = eventBus;
        this.vkClientMiddleware = vkClientMiddleware;
        this.configurationService = configurationService;
        this.eventBus.register(this);
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize() {
        conversations.setOnMouseClicked(event -> Optional.ofNullable(conversations.getSelectionModel())
                .map(MultipleSelectionModel::getSelectedItem)
                .map(ConversationOpenEvent::new)
                .ifPresent(eventBus::post));
        conversations.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                setText((empty || conversation == null || conversation.getTitle() == null) ? null : conversation.getTitle());
            }
        });

        loadAsyncConversationList();
        this.refreshBtn.setOnAction(event -> loadAsyncConversationList());
    }

    public void checkForUpdates() {
        try {
            FXSceneRouter.openModal(Scene.CHECK_FOR_UPDATES_WINDOW);
        } catch (Exception e) {
            UiUtils.notifyError(e);
        }
    }

    public void contactDevelopers() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URL("https://vk.com/im?sel=-195065105").toURI());
            } catch (IOException | URISyntaxException e) {
                UiUtils.notifyError(e);
            }
        }
    }

    public void logout() {
        UiUtils.executeAfterConfirmation("Are you sure that you want to log out?", () -> {
            try {
                var applicationConfiguration = configurationService.loadConfiguration()
                        .orElseGet(ApplicationConfiguration::new);
                applicationConfiguration.setPrincipal(null);
                configurationService.saveConfiguration(applicationConfiguration);
                applicationContext.setPrincipal(null);
                FXSceneRouter.open(Scene.LOGIN_SCENE);
            } catch (Exception e) {
                UiUtils.notifyError(e);
            }
        });
    }

    private void loadAsyncConversationList() {
        this.eventBus.post((ConversationListRefreshEvent) () -> true);
        this.refreshBtn.setDisable(true);
        this.conversations.setVisible(false);
        CompletableFuture.supplyAsync(() -> vkClientMiddleware.loadConversationList(applicationContext.getPrincipal()))
                .whenComplete((result, exception) -> {
                    Platform.runLater(() -> {
                        this.eventBus.post((ConversationListRefreshEvent) () -> false);
                        this.refreshBtn.setDisable(false);

                        if (exception != null) {
                            processApiException(exception);
                            return;
                        }

                        this.conversations.getItems().setAll(result.getItems());
                        this.conversations.setVisible(true);
                    });
                });
    }

    private void processApiException(Throwable exception) {
        if (exception.getCause() instanceof Error) {
            Error vkApiError = (Error) exception.getCause();
            UiUtils.notifyError(vkApiError.getErrorMessage());
            return;
        }

        UiUtils.notifyError(exception);
    }

    @Subscribe
    public void handleConversationSearchEvent(ProgressEvent event) {
        this.conversationListHolder.setDisable(event.isRunning());
        this.menuButton.setDisable(event.isRunning());
    }
}
