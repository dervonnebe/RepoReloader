package top.jaxlabs.repoReloader;

import org.bukkit.plugin.java.JavaPlugin;
import top.jaxlabs.repoReloader.config.PluginConfig;
import top.jaxlabs.repoReloader.github.GitHubClient;
import top.jaxlabs.repoReloader.message.MessageFormatter;
import top.jaxlabs.repoReloader.notify.AdminNotifier;
import top.jaxlabs.repoReloader.update.PendingUpdateRecorder;
import top.jaxlabs.repoReloader.update.UpdateChecker;
import top.jaxlabs.repoReloader.update.UpdateScheduler;

import java.nio.file.Path;

public final class RepoReloader extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginConfig pluginConfig = new PluginConfig(getConfig(), getLogger());

        if (!pluginConfig.hasToken()) {
            getLogger().warning("github-token is empty – update checks disabled. See config.yml for instructions.");
            return;
        }

        if (pluginConfig.repositories().isEmpty()) {
            getLogger().warning("No repositories configured under 'repositories'.");
            return;
        }

        Path pluginsDir = getDataFolder().toPath().getParent() != null
                ? getDataFolder().toPath().getParent()
                : Path.of("plugins");

        GitHubClient gitHubClient = new GitHubClient(pluginConfig.githubToken());
        AdminNotifier notifier = new AdminNotifier(this);
        MessageFormatter messageFormatter = new MessageFormatter(getConfig());
        PendingUpdateRecorder recorder = new PendingUpdateRecorder(getDataFolder().toPath(), getLogger());

        UpdateChecker checker = new UpdateChecker(
                gitHubClient,
                recorder,
                notifier,
                messageFormatter,
                getLogger(),
                pluginsDir,
                getPluginMeta().getVersion()
        );

        new UpdateScheduler(this, checker).scheduleAll(pluginConfig.repositories());

        getLogger().info("RepoReloader started – watching " + pluginConfig.repositories().size() + " repositories.");
    }

    @Override
    public void onDisable() {
        getLogger().info("RepoReloader stopped.");
    }
}
