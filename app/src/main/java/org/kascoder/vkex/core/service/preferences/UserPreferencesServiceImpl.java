package org.kascoder.vkex.core.service.preferences;

import com.google.inject.Inject;
import org.kascoder.vkex.core.model.UserPreferences;
import org.kascoder.vkex.core.service.io.ContentService;
import org.kascoder.vkex.core.util.ApplicationPath;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
public class UserPreferencesServiceImpl implements UserPreferencesService {
    private static final String USER_PREFERENCES_FOLDER_NAME = "preferences";
    private static final String USER_PREFERENCES_FILE_NAME_PREFIX = "preferences_";

    @NonNull
    private final String applicationPath;
    @NonNull
    private final ContentService contentService;

    @Inject
    public UserPreferencesServiceImpl(@ApplicationPath @NonNull String applicationPath,
                                      @NonNull ContentService contentService) {
        this.applicationPath = applicationPath;
        this.contentService = contentService;
    }

    @Override
    public Optional<UserPreferences> loadPreferences(int userId) {
        LOGGER.info("Loading user {} preferences...", userId);
        var userPreferencesFilePath = buildUserPreferencesFilePath(userId);
        if (contentService.exist(userPreferencesFilePath)) {
            var userPreferences = contentService.read(userPreferencesFilePath, UserPreferences.class);
            LOGGER.info("User {} preferences loaded successfully", userId);
            return userPreferences;
        } else {
            LOGGER.info("User {} preferences file doesn't exist", userId);
            return Optional.empty();
        }
    }

    @Override
    public void savePreferences(int userId, @NonNull UserPreferences userPreferences) {
        LOGGER.info("Saving user {} preferences...", userId);
        var userPreferencesFilePath = buildUserPreferencesFilePath(userId);
        contentService.write(userPreferencesFilePath, userPreferences);
        LOGGER.info("User {} preferences saved successfully", userId);
    }

    private String buildUserPreferencesFilePath(int userId) {
        var userPreferencesFileName = USER_PREFERENCES_FILE_NAME_PREFIX + userId;
        var userPreferencesFilePathWithoutExtension = Path.of(applicationPath, USER_PREFERENCES_FOLDER_NAME, userPreferencesFileName);
        return contentService.buildFullFilePath(userPreferencesFilePathWithoutExtension.toString());
    }
}
