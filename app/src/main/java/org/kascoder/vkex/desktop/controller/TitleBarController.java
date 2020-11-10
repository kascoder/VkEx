package org.kascoder.vkex.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.kascoder.vkex.desktop.util.FXSceneRouter;

public class TitleBarController implements ApplicationController {
    @FXML
    private Region region;
    @FXML
    private Label titleLabel;

    private boolean move = false;
    private double lastX = -1, lastY = -1;

    @Override
    public void initialize() {
        this.titleLabel.setText(FXSceneRouter.getMainStageTitle());
    }

    public void close() {
        var stage = FXSceneRouter.currentStage();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void minimize() {
        FXSceneRouter.currentStage().setIconified(true);
    }

    public void startMoveWindow(MouseEvent mouseEvent) {
        this.move = true;
        this.lastX = mouseEvent.getScreenX();
        this.lastY = mouseEvent.getScreenY();
    }

    public void moveWindow(MouseEvent mouseEvent) {
        if (!move) {
            return;
        }

        updateCoordinates(mouseEvent);
    }

    public void endMoveWindow(MouseEvent mouseEvent) {
        if (move) {
            updateCoordinates(mouseEvent);
        }

        resetMoveOperation();
    }

    private void updateCoordinates(MouseEvent mouseEvent) {
        double endMoveX = mouseEvent.getScreenX();
        double endMoveY = mouseEvent.getScreenY();

        Window w = region.getScene().getWindow();

        double stageX = w.getX();
        double stageY = w.getY();

        w.setX(stageX + (endMoveX - lastX));
        w.setY(stageY + (endMoveY - lastY));

        this.lastX = endMoveX;
        this.lastY = endMoveY;
    }

    private void resetMoveOperation() {
        lastX = 0;
        lastY = 0;
        move = false;
    }
}
