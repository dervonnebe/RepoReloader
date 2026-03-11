package top.jaxlabs.repoReloader.config;

import org.bukkit.configuration.file.FileConfiguration;
import top.jaxlabs.repoReloader.model.RepositoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Typed view over the raw {@link FileConfiguration}.
 * Parses and validates all plugin settings on construction so the rest
 * of the code never has to deal with raw YAML objects.
 */
public final class PluginConfig {

    private final String githubToken;
    private final List<RepositoryEntry> repositories;

    public PluginConfig(FileConfiguration config, Logger logger) {
        this.githubToken = config.getString("github-token", "").trim();

        int globalInterval = Math.max(1, config.getInt("check-interval-minutes", 30));
        this.repositories = parseRepositories(config.getList("repositories"), globalInterval, logger);
    }

    public String githubToken() {
        return githubToken;
    }

    public boolean hasToken() {
        return !githubToken.isEmpty();
    }

    public List<RepositoryEntry> repositories() {
        return repositories;
    }

    private List<RepositoryEntry> parseRepositories(List<?> raw, int globalInterval, Logger logger) {
        if (raw == null || raw.isEmpty()) return List.of();

        List<RepositoryEntry> entries = new ArrayList<>();
        for (Object obj : raw) {
            if (!(obj instanceof Map<?, ?> rawMap)) continue;

            String owner = stringOrEmpty(rawMap.get("owner"));
            String repo = stringOrEmpty(rawMap.get("repo"));
            String localFilename = stringOrEmpty(rawMap.get("local-filename"));

            if (owner.isEmpty() || repo.isEmpty() || localFilename.isEmpty()) {
                logger.warning("Skipping repository entry with missing owner/repo/local-filename.");
                continue;
            }

            int interval = globalInterval;
            Object intervalObj = rawMap.get("check-interval-minutes");
            if (intervalObj instanceof Number num) {
                interval = Math.max(1, num.intValue());
            }

            entries.add(new RepositoryEntry(owner, repo, localFilename, interval));
        }
        return List.copyOf(entries);
    }

    private String stringOrEmpty(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
