package top.jaxlabs.repoReloader.update;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.jaxlabs.repoReloader.model.RepositoryEntry;

import java.util.List;
import java.util.logging.Logger;

/**
 * Schedules a separate repeating task for each {@link RepositoryEntry}.
 * Each entry uses its own {@code check-interval-minutes}, which allows
 * high-priority repos to be polled more frequently than others.
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
        long intervalTicks = entry.checkIntervalMinutes() * 60L * 20L;

        new BukkitRunnable() {
            @Override
            public void run() {
                checker.check(entry);
            }
        }.runTaskTimerAsynchronously(plugin, 20L, intervalTicks);

        logger.info("Scheduled check for " + entry.key() + " every " + entry.checkIntervalMinutes() + " min.");
    }
}
