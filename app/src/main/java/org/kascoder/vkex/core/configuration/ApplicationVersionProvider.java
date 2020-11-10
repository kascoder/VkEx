package org.kascoder.vkex.core.configuration;

import com.github.zafarkhaja.semver.Version;
import com.google.inject.Inject;
import com.google.inject.Provider;
import lombok.NonNull;

import javax.inject.Named;

public class ApplicationVersionProvider implements Provider<Version> {
    private final String applicationVersion;

    @Inject
    public ApplicationVersionProvider(@Named("app.version") @NonNull String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Override
    public Version get() {
        return Version.valueOf(applicationVersion);
    }
}
