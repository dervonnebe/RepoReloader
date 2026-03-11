package top.jaxlabs.repoReloader.update;

import org.bukkit.plugin.java.JavaPlugin;
import top.jaxlabs.repoReloader.model.RepositoryEntry;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Schedules a separate repeating async task for each {@link RepositoryEntry}
 * using Folia's {@code AsyncScheduler} (also works on Paper/Spigot).
 * Each entry uses its own {@code check-interval-minutes}.
 */
public final class UpdateScheduler {

    private final JavaPlugin plugin;
    private final UpdateChecker checker;
    private final Logger logger;

    public UpdateScheduler(JavaPlugin plugin, UpdateChecker checker) {
        this.plugin = plugin;
        this.checker = checker;
        this.logger = plugin.getLogger();
    }

    public void scheduleAll(List<RepositoryEntry> repositories) {
        for (RepositoryEntry entry : repositories) {
            scheduleOne(entry);
        }
    }

    private void scheduleOne(RepositoryEntry entry) {
        plugin.getServer().getAsyncScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> checker.check(entry),
                1L,
                entry.checkIntervalMinutes(),
                TimeUnit.MINUTES
        );

        logger.info("Scheduled check for " + entry.key() + " every " + entry.checkIntervalMinutes() + " min.");
    }
}
