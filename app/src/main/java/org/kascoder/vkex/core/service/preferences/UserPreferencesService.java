package org.kascoder.vkex.core.service.preferences;

import lombok.NonNull;
import org.kascoder.vkex.core.model.UserPreferences;

import java.util.Optional;

public interface UserPreferencesService {
    Optional<UserPreferences> loadPreferences(int userId);
    void savePreferences(int userId, @NonNull UserPreferences userPreferences);
}
