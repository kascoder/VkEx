package org.kascoder.vkex.desktop;

import com.google.inject.Guice;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.desktop.configuration.GuiApplicationModule;
import org.kascoder.vkex.desktop.util.FXSceneRouter;
import org.kascoder.vkex.desktop.util.Scene;
import org.kascoder.vkex.desktop.util.UiUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DesktopApplication extends Application {

    private static final double WINDOW_WIDTH = 931;
    private static final double WINDOW_HEIGHT = 600;
    private static final String WINDOW_TITLE = "VkEx";
    public static final EventHandler<WindowEvent> DEFAULT_WINDOW_CLOSE_EVENT_HANDLER = event -> {
        UiUtils.executeAfterConfirmation("Are you sure that you want to exit?", Platform::exit);
        event.consume();
    };
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        var dependencyProvider = Guice.createInjector(new GuiApplicationModule());
        var fxmlLoader = dependencyProvider.getInstance(FXMLLoader.class);
        var applicationContext = dependencyProvider.getInstance(ApplicationContext.class);

        var scene = applicationContext.hasPrincipal() ? Scene.EXPORT_MANAGEMENT_SCENE : Scene.LOGIN_SCENE;

        primaryStage.setOnCloseRequest(DEFAULT_WINDOW_CLOSE_EVENT_HANDLER);
        FXSceneRouter.init(this, primaryStage, fxmlLoader, WINDOW_TITLE, WINDOW_WIDTH, WINDOW_HEIGHT, "/vkex.png");
        FXSceneRouter.startFrom(scene);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
