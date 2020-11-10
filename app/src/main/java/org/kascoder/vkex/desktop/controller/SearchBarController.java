package org.kascoder.vkex.desktop.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.kascoder.vkex.desktop.controller.event.base.ExportEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import org.kascoder.vkex.core.model.options.SearchOptions;
import org.kascoder.vkex.desktop.controller.event.ConversationSelectionResetEvent;
import org.kascoder.vkex.desktop.controller.event.SearchRequestedEvent;
import org.kascoder.vkex.desktop.util.controls.CustomDateCell;
import org.kascoder.vkex.desktop.util.controls.DateTimePicker;
import org.kascoder.vkex.desktop.util.UiUtils;

import java.time.LocalDate;

public class SearchBarController implements ApplicationController {
    @FXML private HBox searchBar;
    @FXML private DateTimePicker fromDateTimePicker;
    @FXML private DateTimePicker toDateTimePicker;
    @FXML private CheckBox fromDateTimeFilterCheck;
    @FXML private CheckBox toDateTimeFilterCheck;

    private final EventBus eventBus;

    @Inject
    public SearchBarController(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    public void initialize() {
        fromDateTimePicker.setDayCellFactory(dateTimePicker -> new CustomDateCell());
        toDateTimePicker.setDayCellFactory(dateTimePicker -> new CustomDateCell());
    }

    public void initiateSearch() {
        try {
            validate();
        } catch (Exception e) {
            UiUtils.notifyError(e.getMessage());
        }

        var searchOptions = buildSearchOptions();
        this.eventBus.post(new SearchRequestedEvent(searchOptions));
    }

    private void validate() throws Exception {
        if (fromDateTimeFilterCheck.isSelected() && fromDateTimePicker.getValue() == null) {
            throw new Exception("From time is empty");
        }

        if (toDateTimeFilterCheck.isSelected() && toDateTimePicker.getValue() == null) {
            throw new Exception("To time is empty");
        }

        if (fromDateTimeFilterCheck.isSelected() && toDateTimeFilterCheck.isSelected()) {
            var from = fromDateTimePicker.getDateTimeValue();
            var to = toDateTimePicker.getDateTimeValue();
            if (from.isAfter(to)) {
                throw new Exception("From date is after To date");
            }
        }
    }

    private SearchOptions buildSearchOptions() {
        SearchOptions options = new SearchOptions();
        if (fromDateTimeFilterCheck.isSelected()) {
            options.setFrom(fromDateTimePicker.getDateTimeValue());
        }
        if (toDateTimeFilterCheck.isSelected()) {
            options.setTo(toDateTimePicker.getDateTimeValue());
        }

        return options;
    }

    public void resetFromDateTimePicker() {
        fromDateTimePicker.setDisable(!fromDateTimeFilterCheck.isSelected());
        fromDateTimePicker.setValue(null);
    }

    public void resetToDateTimePicker() {
        toDateTimePicker.setDisable(!toDateTimeFilterCheck.isSelected());
        if (toDateTimeFilterCheck.isSelected()) {
            toDateTimePicker.setDateTimeValue(LocalDate.now().atTime(23, 59));
        } else {
            toDateTimePicker.setDateTimeValue(null);
        }
    }

    @Subscribe
    private void resetOptions(ConversationSelectionResetEvent event) {
        this.fromDateTimeFilterCheck.setSelected(false);
        this.toDateTimeFilterCheck.setSelected(false);
        resetFromDateTimePicker();
        resetToDateTimePicker();
    }

    @Subscribe
    private void handleExportEvent(ExportEvent event) {
        this.searchBar.setDisable(event.isRunning());
    }
}
