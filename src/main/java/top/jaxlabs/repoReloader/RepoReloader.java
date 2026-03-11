package top.jaxlabs.repoReloader;

import org.bukkit.plugin.java.JavaPlugin;
import top.jaxlabs.repoReloader.command.RepoReloaderCommand;
import top.jaxlabs.repoReloader.config.PluginConfig;
import top.jaxlabs.repoReloader.github.GitHubClient;
import top.jaxlabs.repoReloader.message.MessageFormatter;
import top.jaxlabs.repoReloader.model.RepositoryEntry;
import top.jaxlabs.repoReloader.notify.AdminNotifier;
import top.jaxlabs.repoReloader.update.PendingUpdateRecorder;
import top.jaxlabs.repoReloader.update.UpdateChecker;
import top.jaxlabs.repoReloader.update.UpdateScheduler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RepoReloader extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginConfig pluginConfig = new PluginConfig(getConfig(), getLogger());
        MessageFormatter messageFormatter = new MessageFormatter(getConfig());

        UpdateChecker checker = null;
        List<RepositoryEntry> activeRepositories = List.of();

        if (!pluginConfig.hasToken()) {
            getLogger().warning("github-token is empty - update checks disabled. See config.yml for instructions.");
        } else {
            List<RepositoryEntry> all = new ArrayList<>(pluginConfig.repositories());
            pluginConfig.selfUpdateEntry().ifPresent(entry -> {
                all.add(entry);
                getLogger().info("Self-update enabled - watching dervonnebe/RepoReloader.");
            });

            if (all.isEmpty()) {
                getLogger().warning("No repositories configured and self-update is disabled.");
            } else {
                activeRepositories = List.copyOf(all);

                Path pluginsDir = getDataFolder().toPath().getParent() != null
                        ? getDataFolder().toPath().getParent()
                        : Path.of("plugins");

                GitHubClient gitHubClient = new GitHubClient(pluginConfig.githubToken());
                AdminNotifier notifier = new AdminNotifier(this);
                PendingUpdateRecorder recorder = new PendingUpdateRecorder(getDataFolder().toPath(), getLogger());

                checker = new UpdateChecker(
                        gitHubClient,
                        recorder,
                        notifier,
                        messageFormatter,
                        getLogger(),
                        pluginsDir,
                        getPluginMeta().getVersion()
                );

                new UpdateScheduler(this, checker).scheduleAll(activeRepositories);
                getLogger().info("RepoReloader started - watching " + activeRepositories.size() + " repositories.");
            }
        }

        RepoReloaderCommand rrCommand = new RepoReloaderCommand(this, checker, activeRepositories, messageFormatter);
        Objects.requireNonNull(getCommand("rr")).setExecutor(rrCommand);
        Objects.requireNonNull(getCommand("rr")).setTabCompleter(rrCommand);
    }

    @Override
    public void onDisable() {
        getLogger().info("RepoReloader stopped.");
    }
}
