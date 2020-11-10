package org.kascoder.vkex.core.configuration;

import com.github.zafarkhaja.semver.Version;
import com.google.inject.Inject;
import com.google.inject.Provider;
import lombok.NonNull;
import org.kascoder.vkex.core.service.GithubRepositoryService;
import org.kascoder.vkex.core.service.RepositoryService;
import org.kascoder.vkex.core.util.ApplicationVersion;

import javax.inject.Named;

public class RepositoryServiceProvider implements Provider<RepositoryService> {
    private final Version applicationVersion;
    private final String githubRepositoryPath;

    @Inject
    public RepositoryServiceProvider(@ApplicationVersion Version applicationVersion,
                                     @Named("app.repository.github.path") @NonNull String githubRepositoryPath) {
        this.applicationVersion = applicationVersion;
        this.githubRepositoryPath = githubRepositoryPath;
    }

    @Override
    public RepositoryService get() {
        return new GithubRepositoryService(applicationVersion, githubRepositoryPath);
    }
}
