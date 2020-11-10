package org.kascoder.vkex.core.service.settings;

import com.google.inject.Inject;
import org.kascoder.vkex.core.model.ApplicationConfiguration;
import org.kascoder.vkex.core.service.io.ContentService;
import org.kascoder.vkex.core.util.ApplicationPath;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
public class DefaultConfigurationService implements ConfigurationService {
    private static final String CONFIG_FILE_NAME = "config";

    @NonNull
    private final String applicationPath;
    @NonNull
    private final ContentService contentService;

    @Inject
    public DefaultConfigurationService(@ApplicationPath @NonNull String applicationPath,
                                       @NonNull ContentService contentService) {
        this.applicationPath = applicationPath;
        this.contentService = contentService;
    }

    @Override
    public Optional<ApplicationConfiguration> loadConfiguration() {
        LOGGER.info("Loading application configuration...");
        var configurationFilePath = contentService.buildFullFilePath(Path.of(applicationPath, CONFIG_FILE_NAME).toString());
        if (contentService.exist(configurationFilePath)) {
            var applicationConfiguration = contentService.read(configurationFilePath, ApplicationConfiguration.class);
            LOGGER.info("Application configuration loaded successfully");
            return applicationConfiguration;
        } else {
            LOGGER.info("Application configuration file doesn't exist");
            return Optional.empty();
        }
    }

    @Override
    public void saveConfiguration(@NonNull ApplicationConfiguration configuration) {
        LOGGER.info("Saving application configuration...");
        var configurationFilePath = contentService.buildFullFilePath(Path.of(applicationPath, CONFIG_FILE_NAME).toString());
        contentService.write(configurationFilePath, configuration);
        LOGGER.info("Application configuration saved successfully");
    }
}
