package org.kascoder.vkex.desktop.controller;

import com.google.inject.Inject;
import io.kascoder.vkclient.oauth.OAuthError;
import javafx.scene.control.*;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.ApplicationConfiguration;
import org.kascoder.vkex.core.model.UserPreferences;
import org.kascoder.vkex.core.service.VkClientMiddleware;
import org.kascoder.vkex.core.service.preferences.UserPreferencesService;
import org.kascoder.vkex.core.service.settings.ConfigurationService;
import org.kascoder.vkex.desktop.util.FXSceneRouter;
import org.kascoder.vkex.desktop.util.Scene;
import org.kascoder.vkex.desktop.util.UiUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

public class LoginSceneController implements ApplicationController {

    private final VkClientMiddleware vkClientMiddleware;
    private final ApplicationContext applicationContext;
    private final ConfigurationService configurationService;
    private final UserPreferencesService userPreferencesService;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordPlainTextField;
    @FXML
    private CheckBox showPasswordCheckbox;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private Button loginBtn;
    @FXML
    private ProgressIndicator loadingIndicator;

    @Inject
    public LoginSceneController(VkClientMiddleware vkClientMiddleware,
                                ApplicationContext applicationContext,
                                ConfigurationService configurationService,
                                UserPreferencesService userPreferencesService) {
        this.vkClientMiddleware = vkClientMiddleware;
        this.applicationContext = applicationContext;
        this.configurationService = configurationService;
        this.userPreferencesService = userPreferencesService;
    }

    @Override
    public void initialize() {
        usernameField.textProperty()
                .addListener((observable, oldUsername, newUsername) -> {
                    this.loginBtn.setDisable(!isUsernameValid(newUsername) || !isPasswordValid(getPassword()));
                });

        passwordField.textProperty()
                .addListener((observable, oldPassword, newPassword) -> {
                    this.loginBtn.setDisable(!isPasswordValid(newPassword) || !isUsernameValid(getUsername()));
                });
        passwordField.visibleProperty().bind(showPasswordCheckbox.selectedProperty().not());
        passwordField.managedProperty().bind(showPasswordCheckbox.selectedProperty().not());

        passwordPlainTextField.managedProperty().bind(showPasswordCheckbox.selectedProperty());
        passwordPlainTextField.visibleProperty().bind(showPasswordCheckbox.selectedProperty());
        passwordPlainTextField.textProperty().bindBidirectional(passwordField.textProperty());

        try {
            var principal = this.applicationContext.getPrincipal();
            if (principal != null) {
                FXSceneRouter.open(Scene.EXPORT_MANAGEMENT_SCENE);
            }
        } catch (Exception e) {
            UiUtils.notifyError(e.getMessage());
        }
    }

    public void login() {
        try {
            validate();
        } catch (Exception e) {
            UiUtils.notifyError(e);
            return;
        }

        String username = getUsername();
        String password = getPassword();
        var rememberMe = isRememberMe();

        loadingIndicator.setVisible(true);
        CompletableFuture.supplyAsync(() -> vkClientMiddleware.authenticate(username, password))
                .whenComplete((principal, exception) -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);

                        if (exception != null) {
                            if (exception.getCause() instanceof OAuthError) {
                                OAuthError oAuthError = (OAuthError) exception.getCause();
                                var errorDescription = oAuthError.getErrorDescription();
                                var msg = StringUtils.isBlank(errorDescription) ? oAuthError.getError() : errorDescription;
                                UiUtils.notifyError(msg);
                                return;
                            }

                            UiUtils.notifyError(exception);
                            return;
                        }

                        if (rememberMe) {
                            try {
                                var applicationConfiguration = configurationService.loadConfiguration()
                                        .orElseGet(ApplicationConfiguration::new);
                                applicationConfiguration.setPrincipal(principal);
                                configurationService.saveConfiguration(applicationConfiguration);
                            } catch (Exception e) {
                                UiUtils.notifyError(e);
                                return;
                            }
                        }

                        var userPreferences = userPreferencesService.loadPreferences(principal.getId())
                                .orElse(UserPreferences.DEFAULT);

                        applicationContext.setPrincipal(principal);
                        applicationContext.setUserPreferences(userPreferences);

                        try {
                            FXSceneRouter.open(Scene.EXPORT_MANAGEMENT_SCENE);
                        } catch (Exception e) {
                            UiUtils.notifyError(e);
                        }
                    });
                });
    }

    private void validate() throws Exception {
        String username = getUsername();
        String password = getPassword();

        if (!isUsernameValid(username)) {
            throw new Exception("Username can not be empty.");
        }

        if (!isPasswordValid(password)) {
            throw new Exception("Password can not be empty.");
        }
    }

    private boolean isUsernameValid(String username) {
        return StringUtils.isNotBlank(username);
    }

    private boolean isPasswordValid(String password) {
        return StringUtils.isNotBlank(password);
    }

    private String getUsername() {
        return this.usernameField.getText();
    }

    private String getPassword() {
        return this.passwordField.getText();
    }

    private boolean isRememberMe() {
        return this.rememberMeCheckbox.isSelected();
    }
}
