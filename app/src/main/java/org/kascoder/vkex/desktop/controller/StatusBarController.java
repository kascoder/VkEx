package org.kascoder.vkex.desktop.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.kascoder.vkex.desktop.controller.event.ConversationOpenEvent;
import org.kascoder.vkex.desktop.controller.event.base.ConversationListRefreshEvent;
import org.kascoder.vkex.desktop.controller.event.base.ExportEvent;
import org.kascoder.vkex.desktop.controller.event.base.SearchEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBarController implements ApplicationController {
    @FXML private HBox jobItemsBox;
    @FXML private Label jobCaption;
    @FXML private Label conversationTitle;

    @Inject
    public StatusBarController(EventBus eventBus) {
        eventBus.register(this);
    }

    private void resetJobCaption() {
        this.jobCaption.setText("");
    }

    private void resetConversationTitle() {
        this.conversationTitle.setText("");
    }

    @Subscribe
    private void handleConversationsRefresh(ConversationListRefreshEvent event) {
        Platform.runLater(() -> {
            this.jobItemsBox.setVisible(event.isRunning());
            if (event.isRunning()) {
                resetConversationTitle();
                this.jobCaption.setText("Loading conversations...");
            } else {
                resetJobCaption();
            }
        });
    }

    @Subscribe
    private void handleConversationOpenEvent(ConversationOpenEvent event) {
        var conversation = event.getConversation();
        conversationTitle.setText(conversation.getTitle());
    }

    @Subscribe
    private void handleSearchEvent(SearchEvent event) {
        Platform.runLater(() -> {
            this.jobItemsBox.setVisible(event.isRunning());
            if (event.isRunning()) {
                this.jobCaption.setText("Searching for messages...");
            } else {
                resetJobCaption();
            }
        });
    }

    @Subscribe
    private void handleExportEvent(ExportEvent event) {
        Platform.runLater(() -> {
            this.jobItemsBox.setVisible(event.isRunning());
            if (event.isRunning()) {
                this.jobCaption.setText("Exporting conversation attachments...");
            } else {
                resetJobCaption();
            }
        });
    }
}
