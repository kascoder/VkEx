package org.kascoder.vkex.core.configuration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.kascoder.vkex.core.util.CoreUtils;
import lombok.NonNull;

import javax.inject.Named;

public class ApplicationPathProvider implements Provider<String> {
    private final String applicationPathTemplate;

    @Inject
    public ApplicationPathProvider(@Named("app.path") @NonNull String applicationPathTemplate) {
        this.applicationPathTemplate = applicationPathTemplate;
    }

    @Override
    public String get() {
        return CoreUtils.resolveSystemProperties(applicationPathTemplate);
    }
}
