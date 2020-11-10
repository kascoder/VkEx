package org.kascoder.vkex.core.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.zafarkhaja.semver.Version;
import lombok.NonNull;
import lombok.Value;
import org.kascoder.vkex.core.model.Update;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class GithubRepositoryService implements RepositoryService {
    private final Version applicationVersion;
    private final String githubRepositoryReleasesEndpoint;

    private static final String GITHUB_REPOSITORIES_API_ENDPOINT = "https://api.github.com/repos";

    public GithubRepositoryService(Version applicationVersion, @NonNull String repositoryPath) {
        this.applicationVersion = applicationVersion;
        this.githubRepositoryReleasesEndpoint = String.format("%s%s/releases", GITHUB_REPOSITORIES_API_ENDPOINT, repositoryPath);
    }

    @Override
    public CompletableFuture<Optional<Update>> checkForUpdatesAsync() {
        var httpRequest = HttpRequest.newBuilder(URI.create(githubRepositoryReleasesEndpoint))
                .GET()
                .build();

        return HttpClient.newHttpClient()
                .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::extractReleaseList)
                .thenApply(this::extractUpdate);
    }

    private Optional<Update> extractUpdate(List<GithubRelease> releaseList) {
        if (releaseList.isEmpty()) {
            return Optional.empty();
        }

        var releases= releaseList.stream()
                .sorted(comparing(GithubRelease::getPublishedAt).reversed())
                .collect(toList());

        var latestRelease = releases.get(0);
        if (!latestRelease.hasAssets() || latestRelease.isDraft() || latestRelease.isPrerelease()) {
            return Optional.empty();
        }

        if (!isTagVersionGreaterThanApplicationVersion(latestRelease.getTag())) {
            return Optional.empty();
        }

        boolean backwardCompatible = releases.stream()
                .map(GithubRelease::getTag)
                .filter(Objects::nonNull)
                .filter(this::isTagVersionGreaterThanApplicationVersion)
                .noneMatch(tag -> tag.endsWith("-inc"));

        String appDownloadUrl = null, updaterDownloadUrl = null;
        for (GithubRelease.Asset asset : latestRelease.getAssets()) {
            var assetName = asset.getName();
            if (!assetName.endsWith(".jar")) {
                continue;
            }

            if (assetName.startsWith("vkex-updater")) {
                updaterDownloadUrl = asset.getDownloadUrl();
            } else if (assetName.startsWith("vkex")) {
                appDownloadUrl = asset.getDownloadUrl();
            }
        }

        return Optional.of(new Update(appDownloadUrl, updaterDownloadUrl, backwardCompatible));
    }

    private boolean isTagVersionGreaterThanApplicationVersion(String tag) {
        return extractVersion(tag).greaterThan(applicationVersion);
    }

    private List<GithubRelease> extractReleaseList(String content) {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            return mapper.readValue(content, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Version extractVersion(@NonNull String tag) {
        String versionRaw = tag;
        if (versionRaw.startsWith("v")) {
            versionRaw = versionRaw.substring(1);
        }
        if (versionRaw.endsWith("-inc")) {
            versionRaw = versionRaw.substring(0, versionRaw.length() - 4);
        }

        return Version.valueOf(versionRaw);
    }

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GithubRelease {
        long id;
        String url;
        String tag;
        String name;
        String body;
        boolean draft;
        String assetsUrl;
        boolean prerelease;
        ZonedDateTime createdAt;
        ZonedDateTime publishedAt;
        List<Asset> assets;

        @JsonCreator
        public GithubRelease(@JsonProperty("id") long id, @JsonProperty("url") String url,
                             @JsonProperty("assets_url") String assetsUrl, @JsonProperty("tag_name") String tag,
                             @JsonProperty("name") String name, @JsonProperty("body") String body,
                             @JsonProperty("draft") boolean draft, @JsonProperty("prerelease") boolean prerelease,
                             @JsonProperty("created_at") ZonedDateTime createdAt, @JsonProperty("published_at") ZonedDateTime publishedAt,
                             @JsonProperty("assets") List<Asset> assets) {
            this.id = id;
            this.url = url;
            this.assetsUrl = assetsUrl;
            this.tag = tag;
            this.name = name;
            this.body = body;
            this.draft = draft;
            this.prerelease = prerelease;
            this.createdAt = createdAt;
            this.publishedAt = publishedAt;
            this.assets = assets != null ? assets : new ArrayList<>();
        }

        public boolean hasAssets() {
            return !assets.isEmpty();
        }

        @Value
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Asset {
            long id;
            long size;
            String url;
            String name;
            String label;
            String state;
            String downloadUrl;
            int downloadCount;
            String contentType;

            public Asset(@JsonProperty("id") long id, @JsonProperty("size") long size,
                         @JsonProperty("url") String url, @JsonProperty("name") String name,
                         @JsonProperty("label") String label, @JsonProperty("state") String state,
                         @JsonProperty("browser_download_url") String downloadUrl, @JsonProperty("download_count") int downloadCount,
                         @JsonProperty("content_type") String contentType) {
                this.id = id;
                this.size = size;
                this.url = url;
                this.name = name;
                this.label = label;
                this.state = state;
                this.downloadUrl = downloadUrl;
                this.downloadCount = downloadCount;
                this.contentType = contentType;
            }
        }
    }
}
