package org.kascoder.vkex.desktop.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import io.kascoder.vkclient.Error;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.Conversation;
import org.kascoder.vkex.core.model.ExportResultObject;
import org.kascoder.vkex.core.model.options.ExportOptions;
import org.kascoder.vkex.core.model.options.StorageOptions;
import org.kascoder.vkex.core.model.source.MessageSource;
import org.kascoder.vkex.core.model.util.MessageSourceClosedException;
import org.kascoder.vkex.core.model.util.StorageType;
import org.kascoder.vkex.core.service.VkClientMiddleware;
import org.kascoder.vkex.core.service.export.ConversationExportService;
import org.kascoder.vkex.core.service.io.attachment.AttachmentDownloadServiceFactory;
import org.kascoder.vkex.core.util.ExportContext;
import org.kascoder.vkex.desktop.DesktopApplication;
import org.kascoder.vkex.desktop.controller.event.*;
import org.kascoder.vkex.desktop.controller.event.base.ConversationListRefreshEvent;
import org.kascoder.vkex.desktop.controller.event.base.ExportEvent;
import org.kascoder.vkex.desktop.controller.event.base.SearchEvent;
import org.kascoder.vkex.desktop.util.FXSceneRouter;
import org.kascoder.vkex.desktop.util.Scene;
import org.kascoder.vkex.desktop.util.UiUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ExportLayoutController implements ApplicationController {

    @FXML VBox layout;

    private ExportStatus exportStatus;
    private Conversation conversation;
    private MessageSource messageSource;
    private final EventBus eventBus;
    private final ApplicationContext applicationContext;
    private final VkClientMiddleware vkClientMiddleware;
    private final ConversationExportService conversationExportService;

    @Inject
    public ExportLayoutController(EventBus eventBus,
                                  ApplicationContext applicationContext,
                                  VkClientMiddleware vkClientMiddleware,
                                  ConversationExportService conversationExportService) {
        this.eventBus = eventBus;
        this.conversationExportService = conversationExportService;
        this.eventBus.register(this);
        this.exportStatus = ExportStatus.NONE;
        this.applicationContext = applicationContext;
        this.vkClientMiddleware = vkClientMiddleware;

        FXSceneRouter.currentStage()
                .setOnCloseRequest(event -> {
                    if (ExportStatus.IN_PROGRESS.equals(exportStatus)) {
                        UiUtils.notifyError("Export in progress. Stop it to exit.");
                        event.consume();
                    } else {
                        DesktopApplication.DEFAULT_WINDOW_CLOSE_EVENT_HANDLER.handle(event);
                    }
                });
    }

    private void reset() {
        eventBus.post(new ConversationSelectionResetEvent());
    }

    private void postExport(StorageOptions storageOptions, ExportResultObject exportResultObject) {
        try {
            var props = new HashMap<String, Object>();
            props.put("resultObject", exportResultObject);
            props.put("storageOptions", storageOptions);
            FXSceneRouter.openModal(Scene.EXPORT_RESULT_WINDOW, props);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            UiUtils.notifyError(e);
        }
    }

    private boolean isDifferentConversation(Integer anotherConversationId) {
        return !Objects.equals(getConversationId(), anotherConversationId);
    }

    private void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    private Integer getConversationId() {
        return conversation == null ? null : conversation.getId();
    }

    @Subscribe
    public void handleExportStoppedEvent(ExportStoppedEvent event) {
        if (messageSource != null) {
            messageSource.close();
        }
    }

    @Subscribe
    private void handleExportRequestedEvent(ExportRequestedEvent event) {
        this.messageSource = event.getMessageSource();
        @NonNull var exportTypes = event.getExportTypes();
        @NonNull var progressNotifier = event.getProgressNotifier();


        CompletableFuture.runAsync(() -> {
            eventBus.post((ExportEvent) () -> true);
            var downloadPath = event.getDownloadPath();
            if (event.isWrapPath()) {
                downloadPath = Path.of(event.getDownloadPath(), conversation.getTitle()).toString();
            }

            var exportOptions = new ExportOptions(conversation, exportTypes, messageSource, null);
            var storageOptions = new StorageOptions(StorageType.LOCAL, downloadPath);
            var attachmentDownloadService = AttachmentDownloadServiceFactory.build(storageOptions);
            var exportContext = new ExportContext(applicationContext.getPrincipal(), exportOptions, attachmentDownloadService);
            var exportStatistics = conversationExportService.export(exportContext, progressNotifier);
            eventBus.post((ExportEvent) () -> false);
            Platform.runLater(() -> postExport(storageOptions, exportStatistics));
        }).exceptionally(e -> {
            if (e.getCause() instanceof MessageSourceClosedException) {
                LOGGER.info("Export was interrupted manually");
            } else {

                LOGGER.error(e.getMessage(), e);
                Platform.runLater(() -> UiUtils.notifyError(e.getMessage()));
            }

            eventBus.post((ExportEvent) () -> false);
            return null;
        });
    }

    @Subscribe
    private void handleExportEvent(ExportEvent event) {
        this.exportStatus = event.isRunning() ? ExportStatus.IN_PROGRESS : ExportStatus.NONE;
    }

    @Subscribe
    public void handleConversationListRefreshEvent(ConversationListRefreshEvent event) {
        if (!event.isRunning()) {
            return;
        }

        this.layout.setVisible(false);
        reset();
    }

    @Subscribe
    private void handleConversationOpenEvent(ConversationOpenEvent event) {
        var conversation = event.getConversation();

        this.layout.setVisible(true);
        if (isDifferentConversation(conversation.getId())) {
            reset();
        }
        setConversation(conversation);
    }

    @Subscribe
    private void handleSearchEvent(SearchEvent event) {
        this.layout.setDisable(event.isRunning());
    }

    @Subscribe
    private void handleSearchRequestedEvent(SearchRequestedEvent event) {
        CompletableFuture.runAsync(() -> {
            eventBus.post((SearchEvent) () -> true);
            var searchOptions = event.getSearchOptions();
            searchOptions.setConversationId(getConversationId());

            var messages = vkClientMiddleware.searchForMessages(applicationContext.getPrincipal(), searchOptions);
            eventBus.post(new SearchCompletedEvent(messages));
        }).exceptionally(e -> {
            String msg;
            if (e instanceof Error) {
                Error error = (Error) e;
                msg = error.getErrorMessage();
            } else {
                msg = e.getMessage();
                LOGGER.error(msg, e);
            }

            eventBus.post((SearchEvent) () -> false);
            Platform.runLater(() -> UiUtils.notifyError(msg));
            return null;
        });
    }

    private enum ExportStatus {
        NONE, IN_PROGRESS
    }
}
