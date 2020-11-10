package org.kascoder.vkex.desktop.util;

import org.kascoder.vkex.desktop.util.FXSceneRouter.FXScene;

public enum Scene implements FXScene {
    LOGIN_SCENE("/ui/view/loginScene.fxml"),
    EXPORT_MANAGEMENT_SCENE("/ui/view/exportManagementScene.fxml"),
    EXPORT_RESULT_WINDOW("/ui/view/exportResultWindow.fxml", "Summary", null, true, true, false, 550.0, 300.0),
    CHECK_FOR_UPDATES_WINDOW("/ui/view/checkForUpdatesWindow.fxml", null, null, true, true, false, 300.0, 100.0);

    private final String path;
    private final String title;
    private final String iconPath;
    private final boolean useIcon;
    private final boolean useTitle;
    private final boolean resizable;
    private final Double width;
    private final Double height;

    Scene(String path) {
        this(path, null, null, true, true, false, null, null);
    }

    Scene(String path, String title, String iconPath, boolean useIcon, boolean useTitle, boolean resizable, Double width, Double height) {
        this.path = path;
        this.title = title;
        this.iconPath = iconPath;
        this.useIcon = useIcon;
        this.useTitle = useTitle;
        this.resizable = resizable;
        this.width = width;
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public String getIconPath() {
        return iconPath;
    }

    public boolean isUseIcon() {
        return useIcon;
    }

    public boolean isUseTitle() {
        return useTitle;
    }

    public boolean isResizable() {
        return resizable;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
        return height;
    }
}
