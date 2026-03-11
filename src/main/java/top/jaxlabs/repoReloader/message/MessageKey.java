package top.jaxlabs.repoReloader.message;

/**
 * All player-facing messages. Config path and MiniMessage default template per key.
 *
 * Available placeholders per message:
 *  UPDATE_FOUND  – {repo}, {old}, {new}, {file}
 *  DOWNLOAD_FAILED – {repo}, {error}
 */
public enum MessageKey {

    UPDATE_FOUND(
            "messages.update-found",
            "<green>[RepoReloader]</green> Update für <aqua>{repo}</aqua> gefunden"
                    + " (<red>{old}</red> <gray>→</gray> <green>{new}</green>)"
                    + " – <yellow>{file}</yellow> heruntergeladen. Neustart erforderlich."
    ),
    DOWNLOAD_FAILED(
            "messages.download-failed",
            "<red>[RepoReloader]</red> <white>Download fehlgeschlagen für"
                    + " <aqua>{repo}</aqua>: {error}</white>"
    );

    private final String configPath;
    private final String defaultTemplate;

    MessageKey(String configPath, String defaultTemplate) {
        this.configPath = configPath;
        this.defaultTemplate = defaultTemplate;
    }

    public String configPath() {
        return configPath;
    }

    public String defaultTemplate() {
        return defaultTemplate;
    }
}
