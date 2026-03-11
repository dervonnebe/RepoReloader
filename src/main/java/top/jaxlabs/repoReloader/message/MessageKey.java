package top.jaxlabs.repoReloader.message;

/**
 * All player-facing messages. Config path and MiniMessage default template per key.
 *
 * Available placeholders per message:
 *  UPDATE_FOUND        - {repo}, {old}, {new}, {file}
 *  DOWNLOAD_FAILED     - {repo}, {error}
 *  CMD_CHECK_ALL       - {count}
 *  CMD_CHECK_ONE       - {repo}
 *  CMD_REPO_NOT_FOUND  - {repo}
 *  CMD_NOT_INITIALIZED - (none)
 *  CMD_NO_PERMISSION   - (none)
 *  CMD_USAGE           - (none)
 */
public enum MessageKey {

    UPDATE_FOUND(
            "messages.update-found",
            "<green>[RepoReloader]</green> Update für <aqua>{repo}</aqua> gefunden"
                    + " (<red>{old}</red> <gray>→</gray> <green>{new}</green>)"
                    + " - <yellow>{file}</yellow> heruntergeladen. Neustart erforderlich."
    ),
    DOWNLOAD_FAILED(
            "messages.download-failed",
            "<red>[RepoReloader]</red> <white>Download fehlgeschlagen für"
                    + " <aqua>{repo}</aqua>: {error}</white>"
    ),
    CMD_CHECK_ALL(
            "messages.cmd-check-all",
            "<green>[RepoReloader]</green> Force-Check gestartet für <white>{count}</white> Repositories..."
    ),
    CMD_CHECK_ONE(
            "messages.cmd-check-one",
            "<green>[RepoReloader]</green> Force-Check gestartet für <aqua>{repo}</aqua>..."
    ),
    CMD_REPO_NOT_FOUND(
            "messages.cmd-repo-not-found",
            "<red>[RepoReloader]</red> Repository <aqua>{repo}</aqua> nicht in der Konfiguration gefunden."
    ),
    CMD_NOT_INITIALIZED(
            "messages.cmd-not-initialized",
            "<red>[RepoReloader]</red> Plugin nicht initialisiert (Token oder Repositories fehlen)."
    ),
    CMD_NO_PERMISSION(
            "messages.cmd-no-permission",
            "<red>[RepoReloader]</red> Du hast keine Berechtigung für diesen Befehl."
    ),
    CMD_USAGE(
            "messages.cmd-usage",
            "<gray>[RepoReloader]</gray> Verwendung: <white>/rr check [owner/repo]</white>"
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
