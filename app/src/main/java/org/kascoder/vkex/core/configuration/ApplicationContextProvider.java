package org.kascoder.vkex.core.configuration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.SimpleApplicationContext;
import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.UserPreferences;
import org.kascoder.vkex.core.service.preferences.UserPreferencesService;
import org.kascoder.vkex.core.service.settings.ConfigurationService;

import java.util.Set;

public class ApplicationContextProvider implements Provider<ApplicationContext> {
    private final ConfigurationService configurationService;
    private final UserPreferencesService userPreferencesService;

    @Inject
    public ApplicationContextProvider(ConfigurationService configurationService,
                                      UserPreferencesService userPreferencesService) {
        this.configurationService = configurationService;
        this.userPreferencesService = userPreferencesService;
    }

    @Override
    public ApplicationContext get() {
        var applicationContext = new SimpleApplicationContext();

        configurationService.loadConfiguration()
                .ifPresent(configuration -> {
                    var principal = configuration.getPrincipal();
                    if (principal != null) {
                        applicationContext.setPrincipal(principal);
                        var userId = principal.getId();
                        var userPreferences = userPreferencesService.loadPreferences(userId)
                                .map(preferences -> {
                                    if (preferences.getExportTypes() == null) {
                                        preferences.setExportTypes(Set.of(ExportType.values()));
                                    }

                                    return preferences;
                                })
                                .orElse(UserPreferences.DEFAULT);
                        applicationContext.setUserPreferences(userPreferences);
                    }
                });

        return applicationContext;
    }
}
