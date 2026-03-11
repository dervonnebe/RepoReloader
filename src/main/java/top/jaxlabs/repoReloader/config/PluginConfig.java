package top.jaxlabs.repoReloader.config;

import org.bukkit.configuration.file.FileConfiguration;
import top.jaxlabs.repoReloader.model.RepositoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Typed view over the raw {@link FileConfiguration}.
 * Parses and validates all plugin settings on construction so the rest
 * of the code never has to deal with raw YAML objects.
 */
public final class PluginConfig {

    private static final String SELF_OWNER = "dervonnebe";
    private static final String SELF_REPO  = "RepoReloader";

    private final String githubToken;
    private final int globalInterval;
    private final List<RepositoryEntry> repositories;
    private final Optional<RepositoryEntry> selfUpdateEntry;

    public PluginConfig(FileConfiguration config, Logger logger) {
        this.githubToken   = config.getString("github-token", "").trim();
        this.globalInterval = Math.max(1, config.getInt("check-interval-minutes", 30));
        this.repositories  = parseRepositories(config.getList("repositories"), logger);
        this.selfUpdateEntry = parseSelfUpdate(config);
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

    /** Present when {@code self-update.enabled: true} is set in config. */
    public Optional<RepositoryEntry> selfUpdateEntry() {
        return selfUpdateEntry;
    }

    // -------------------------------------------------------------------------

    private Optional<RepositoryEntry> parseSelfUpdate(FileConfiguration config) {
        if (!config.getBoolean("self-update.enabled", false)) {
            return Optional.empty();
        }

        String owner    = config.getString("self-update.owner", SELF_OWNER).trim();
        String repo     = config.getString("self-update.repo",  SELF_REPO).trim();
        int    interval = Math.max(1, config.getInt("self-update.check-interval-minutes", globalInterval));

        return Optional.of(new RepositoryEntry(owner, repo, SELF_REPO + ".jar", interval));
    }

    private List<RepositoryEntry> parseRepositories(List<?> raw, Logger logger) {
        if (raw == null || raw.isEmpty()) return List.of();

        List<RepositoryEntry> entries = new ArrayList<>();
        for (Object obj : raw) {
            if (!(obj instanceof Map<?, ?> rawMap)) continue;

            String owner         = stringOrEmpty(rawMap.get("owner"));
            String repo          = stringOrEmpty(rawMap.get("repo"));
            String localFilename = resolveFilename(rawMap);

            if (owner.isEmpty() || repo.isEmpty() || localFilename.isEmpty()) {
                logger.warning("Skipping repository entry with missing owner/repo/plugin-name.");
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

    /**
     * Resolves the local JAR filename from a config entry.
     * Prefers {@code plugin-name} (no extension needed) over the legacy
     * {@code local-filename} key. A trailing {@code .jar} is always normalised.
     */
    private String resolveFilename(Map<?, ?> rawMap) {
        String name = stringOrEmpty(rawMap.get("plugin-name"));
        if (name.isEmpty()) {
            name = stringOrEmpty(rawMap.get("local-filename"));
        }
        if (name.isEmpty()) return "";

        if (name.toLowerCase().endsWith(".jar")) {
            name = name.substring(0, name.length() - 4);
        }
        return name + ".jar";
    }

    private String stringOrEmpty(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
