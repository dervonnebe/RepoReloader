package top.jaxlabs.repoReloader.update;

import top.jaxlabs.repoReloader.github.GitHubClient;
import top.jaxlabs.repoReloader.message.MessageFormatter;
import top.jaxlabs.repoReloader.message.MessageKey;
import top.jaxlabs.repoReloader.model.ReleaseAsset;
import top.jaxlabs.repoReloader.model.ReleaseInfo;
import top.jaxlabs.repoReloader.model.RepositoryEntry;
import top.jaxlabs.repoReloader.notify.AdminNotifier;
import top.jaxlabs.repoReloader.version.ComparableVersion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Checks a single {@link RepositoryEntry} for a newer GitHub release,
 * downloads the asset if one is found, and notifies online admins.
 * Keeps track of already-notified tags to avoid duplicate notifications across
 * repeated scheduler invocations.
 */
public final class UpdateChecker {

    private final GitHubClient gitHubClient;
    private final PendingUpdateRecorder recorder;
    private final AdminNotifier notifier;
    private final MessageFormatter messageFormatter;
    private final Logger logger;
    private final Path pluginsDir;
    private final String currentVersion;

    private final Map<String, String> lastNotifiedTagByRepo = new HashMap<>();

    public UpdateChecker(
            GitHubClient gitHubClient,
            PendingUpdateRecorder recorder,
            AdminNotifier notifier,
            MessageFormatter messageFormatter,
            Logger logger,
            Path pluginsDir,
            String currentVersion
    ) {
        this.gitHubClient = gitHubClient;
        this.recorder = recorder;
        this.notifier = notifier;
        this.messageFormatter = messageFormatter;
        this.logger = logger;
        this.pluginsDir = pluginsDir;
        this.currentVersion = currentVersion;
    }

    public void check(RepositoryEntry entry) {
        String key = entry.key();
        try {
            ReleaseInfo release = gitHubClient.fetchLatestRelease(entry.owner(), entry.repo());

            if (!ComparableVersion.isNewer(release.tagName(), currentVersion)) return;
            if (release.tagName().equalsIgnoreCase(lastNotifiedTagByRepo.get(key))) return;

            Optional<ReleaseAsset> assetOpt = resolveAsset(release, entry.localFilename());
            if (assetOpt.isEmpty()) {
                logger.warning("No matching release asset for " + key + " (expected filename: " + entry.localFilename() + ")");
                return;
            }

            ReleaseAsset asset = assetOpt.get();
            Path destination = pluginsDir.resolve(entry.localFilename() + ".update");
            downloadAsset(asset, destination);

            recorder.record(key, release.tagName(), destination);
            lastNotifiedTagByRepo.put(key, release.tagName());

            Map<String, String> placeholders = Map.of(
                    "repo", key,
                    "old", currentVersion,
                    "new", release.tagName(),
                    "file", destination.getFileName().toString()
            );
            notifier.notifyAdmins(messageFormatter.format(MessageKey.UPDATE_FOUND, placeholders));
            logger.info("Update for " + key + ": " + currentVersion + " -> " + release.tagName()
                    + " | downloaded to " + destination.getFileName());

        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) Thread.currentThread().interrupt();

            Map<String, String> placeholders = Map.of(
                    "repo", key,
                    "error", String.valueOf(exception.getMessage())
            );
            notifier.notifyAdmins(messageFormatter.format(MessageKey.DOWNLOAD_FAILED, placeholders));
            logger.warning("Update check failed for " + key + ": " + exception.getMessage());
        }
    }

    /**
     * Prefers an exact filename match, falls back to the first {@code .jar} asset.
     */
    private Optional<ReleaseAsset> resolveAsset(ReleaseInfo release, String localFilename) {
        for (ReleaseAsset asset : release.assets()) {
            if (asset.name().equalsIgnoreCase(localFilename)) return Optional.of(asset);
        }
        for (ReleaseAsset asset : release.assets()) {
            if (asset.name().toLowerCase().endsWith(".jar")) return Optional.of(asset);
        }
        return Optional.empty();
    }

    private void downloadAsset(ReleaseAsset asset, Path destination) throws IOException, InterruptedException {
        Files.createDirectories(destination.getParent());
        try (InputStream stream = gitHubClient.downloadAsset(asset.apiUrl())) {
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
