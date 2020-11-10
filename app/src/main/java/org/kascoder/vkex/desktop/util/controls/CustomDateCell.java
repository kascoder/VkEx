package org.kascoder.vkex.desktop.util.controls;

import javafx.scene.control.DateCell;

import java.time.LocalDate;

public class CustomDateCell extends DateCell {
    @Override
    public void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);

        if (item.isAfter(LocalDate.now())) {
            setDisable(true);
            setStyle("-fx-background-color: #ffc0cb;");
        }
    }
}
