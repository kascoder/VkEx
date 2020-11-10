package org.kascoder.vkex.cli.command.preferences;

import com.google.inject.Inject;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.options.StorageOptions;
import org.kascoder.vkex.core.model.util.StorageType;
import org.kascoder.vkex.core.service.preferences.UserPreferencesService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@Command(name = "prefs")
public class PreferencesCommand implements Runnable {
    private final ApplicationContext applicationContext;
    private final UserPreferencesService userPreferencesService;

    @Inject
    public PreferencesCommand(ApplicationContext applicationContext, UserPreferencesService userPreferencesService) {
        this.applicationContext = applicationContext;
        this.userPreferencesService = userPreferencesService;
    }

    @Option(names = "--export-path")
    private Path defaultExportPath;

    @Option(
            names = "--types",
            split = ",",
            description = "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private Set<ExportType> exportTypes;

    @Option(names = "--display")
    private boolean display;

    @Override
    public void run() {
        if (!applicationContext.hasPrincipal()) {
            throw new RuntimeException("You are not logged in");
        }

        var userPreference = applicationContext.getUserPreferences();
        if (display) {
            LOGGER.warn(userPreference.toString());
            return;
        }

        if (defaultExportPath != null) {
            if (Files.notExists(defaultExportPath)) {
                try {
                    Files.createDirectory(defaultExportPath);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    return;
                }
            } else if (!Files.isDirectory(defaultExportPath)) {
                throw new RuntimeException("The path should direct to folder");
            }

            userPreference.setStorageOptions(new StorageOptions(StorageType.LOCAL, defaultExportPath.toString()));
        }

        if (isNotEmptyExportTypes()) {
            userPreference.setExportTypes(exportTypes);
        }

        if (shouldPreferencesBeSaved()) {
            var principal = applicationContext.getPrincipal();
            userPreferencesService.savePreferences(principal.getId(), userPreference);
        }
    }

    private boolean shouldPreferencesBeSaved() {
        return defaultExportPath != null || isNotEmptyExportTypes();
    }

    private boolean isNotEmptyExportTypes() {
        return exportTypes != null && !exportTypes.isEmpty();
    }
}
