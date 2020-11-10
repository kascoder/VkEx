package org.kascoder.vkex.desktop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.NonNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static java.util.Collections.emptyMap;

public final class FXSceneRouter {
    private static FXSceneRouter instance;

    private final Object ref;
    private final Stage mainStage;
    private final FXMLLoader fxmlLoader;
    private final String mainStageTitle;
    private final double mainStageWidth;
    private final double mainStageHeight;
    private final String mainStageIconPath;

    private Stage currentStage;

    private FXSceneRouter(Object ref, Stage mainStage, FXMLLoader fxmlLoader, String mainStageTitle, double mainStageWidth, double mainStageHeight, String mainStageIconPath) {
        this.ref = ref;
        this.mainStage = mainStage;
        this.fxmlLoader = fxmlLoader;
        this.mainStageTitle = mainStageTitle;
        this.mainStageWidth = mainStageWidth;
        this.mainStageHeight = mainStageHeight;
        this.mainStageIconPath = mainStageIconPath;
        mainStage.initStyle(StageStyle.UNDECORATED);
    }

    public static void init(Object ref, Stage mainStage, String mainStageTitle,
                            double mainStageWidth, double mainStageHeight, String mainStageIconPath) {
        init(ref, mainStage, new FXMLLoader(), mainStageTitle, mainStageWidth, mainStageHeight, mainStageIconPath);
    }

    public static void init(@NonNull Object ref, @NonNull Stage mainStage, @NonNull FXMLLoader fxmlLoader,
                            String mainStageTitle, double mainStageWidth, double mainStageHeight, String mainStageIconPath) {
        if (instance != null) {
            throw new RuntimeException("Router is already initialized");
        }

        instance = new FXSceneRouter(ref, mainStage, fxmlLoader, mainStageTitle, mainStageWidth, mainStageHeight, mainStageIconPath);
    }

    public static Stage currentStage() {
        validateInitialization();
        return instance.currentStage;
    }

    private static void validateInitialization() {
        if (instance == null) {
            throw new RuntimeException("Router isn't initialized");
        }
    }

    //------------------------------------------------------ Open ------------------------------------------------------

    public static void startFrom(FXScene scene) throws Exception {
        startFrom(scene, emptyMap());
    }

    public static void startFrom(FXScene scene, Map<String, Object> props) throws Exception {
        open(scene, props);
    }

    public static void open(FXScene scene) throws Exception {
        open(scene, emptyMap());
    }

    public static void open(FXScene scene, Map<String, Object> props) throws Exception {
        open(scene, false, props);
    }

    public static void openModal(FXScene scene) throws Exception {
        openModal(scene, emptyMap());
    }

    public static void openModal(FXScene scene, Map<String, Object> props) throws Exception {
        open(scene, true, props);
    }

    private static void open(FXScene scene, boolean modal, Map<String, Object> props) throws Exception {
        validateInitialization();
        instance.loadNewScene(scene, modal, props);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void loadNewScene(@NonNull FXScene scene, boolean modal, @NonNull Map<String, Object> props) throws Exception {
        Stage currentWindow;
        if (modal) {
            currentWindow = new Stage();
            currentWindow.initModality(Modality.APPLICATION_MODAL);
            currentWindow.setOnHiding(event -> instance.currentStage = mainStage);
        } else {
            currentWindow = mainStage;
        }

        currentWindow.getIcons().clear();
        if (scene.isUseIcon()) {
            var iconPath = (scene.getIconPath() == null) ? instance.mainStageIconPath : scene.getIconPath();
            if (iconPath != null) {
                InputStream iconStream = instance.ref.getClass()
                        .getResourceAsStream(iconPath);
                Image image = new Image(iconStream);
                currentWindow.getIcons().add(image);
            }
        }

        var title = scene.isUseTitle()
                ? scene.getTitle() == null ? instance.mainStageTitle : scene.getTitle()
                : null;
        var resizable = scene.isResizable();
        currentWindow.setTitle(title);
        currentWindow.setResizable(resizable);

        var width = scene.getWidth() == null ? instance.mainStageWidth : scene.getWidth();
        var height = scene.getHeight() == null ? instance.mainStageHeight : scene.getHeight();

        var currentWindowProperties = currentWindow.getProperties();
        props.forEach(currentWindowProperties::put);
        instance.currentStage = currentWindow;

        URL xmlUrl = instance.ref.getClass().getResource(scene.getPath());

        fxmlLoader.setRoot(null);
        fxmlLoader.setController(null);
        fxmlLoader.setLocation(xmlUrl);
        Parent resource = fxmlLoader.load();
        currentWindow.setScene(new javafx.scene.Scene(resource, width, height));
        currentWindow.show();
    }

    public static String getMainStageTitle() {
        return instance.mainStageTitle;
    }

    public interface FXScene {
        String getIconPath();
        boolean isUseIcon();
        boolean isUseTitle();
        @NonNull
        String getPath();
        boolean isResizable();
        String getTitle();
        Double getWidth();
        Double getHeight();
    }
}
