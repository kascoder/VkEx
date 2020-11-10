package org.kascoder.vkex.core.service.settings;

import org.kascoder.vkex.core.model.ApplicationConfiguration;
import lombok.NonNull;

import java.util.Optional;

public interface ConfigurationService {

    Optional<ApplicationConfiguration> loadConfiguration();

    void saveConfiguration(@NonNull ApplicationConfiguration configuration);
}
