package org.kascoder.vkex.desktop.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class UiUtils {
    private static final String DEFAULT_ALERT_HEADER = "";

    private UiUtils() {

    }

    public static void notifyError(Throwable throwable) {
        notifyError( throwable.getMessage());
    }

    public static void notifyWarning(Throwable throwable) {
        notifyWarning( throwable.getMessage());
    }

    public static void notifyError(String content) {
        notifyError(DEFAULT_ALERT_HEADER, content);
    }

    public static void notifyWarning(String content) {
        notifyWarning(DEFAULT_ALERT_HEADER, content);
    }

    public static void notifyInfo(String content) {
        notifyInfo(DEFAULT_ALERT_HEADER, content);
    }

    public static void notifyError(String header, String content) {
        notifyEvent(header, content, Alert.AlertType.ERROR);
    }

    public static void notifyWarning(String header, String content) {
        notifyEvent(header, content, Alert.AlertType.WARNING);
    }

    public static void notifyInfo(String header, String content) {
        notifyEvent(header, content, Alert.AlertType.INFORMATION);
    }

    public static void executeAfterConfirmation(String question, Runnable task) {
        showConfirmation(question, buttonType -> {
            if (ButtonType.YES.equals(buttonType)) {
                task.run();
            }
        });
    }

    public static void showConfirmation(String question, Consumer<ButtonType> userActionHandler) {
        showConfirmation(question, ButtonType.YES, ButtonType.NO, userActionHandler);
    }

    public static void showConfirmation(String question, ButtonType yesBtn, ButtonType noBtn, Consumer<ButtonType> userActionHandler) {
        Alert confirmation = buildAlert(null);
        confirmation.setHeaderText(DEFAULT_ALERT_HEADER);
        confirmation.setContentText(question);
        confirmation.getButtonTypes().add(yesBtn);
        confirmation.getButtonTypes().add(noBtn);
        confirmation.initOwner(FXSceneRouter.currentStage());
        confirmation.showAndWait()
                .ifPresent(userActionHandler);
    }

    private static void notifyEvent(String header, String content, Alert.AlertType alertType) {
        Alert errorAlert = buildAlert(alertType);
        errorAlert.setHeaderText(header);
        errorAlert.setContentText(content);
        errorAlert.initOwner(FXSceneRouter.currentStage());
        errorAlert.show();
    }

    public static void openDirectoryInExplorer(String directoryPath) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        var directory = new File(directoryPath);
        if (!directory.exists()) {
            throw new RuntimeException("Directory doesn't exist");
        }

        if (!directory.isDirectory()) {
            throw new RuntimeException("Provided path doesn't direct to directory");
        }

        var desktop = Desktop.getDesktop();
        try {
            desktop.open(directory);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Alert buildAlert(Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        var dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(UiUtils.class.getResource("/ui/css/alert.css").toExternalForm());
        return alert;
    }
}
