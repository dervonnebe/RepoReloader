package top.jaxlabs.repoReloader.notify;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import top.jaxlabs.repoReloader.permission.PluginPermission;

/**
 * Broadcasts a {@link Component} message to all online players who hold
 * the {@link PluginPermission#ADMIN_UPDATES} permission.
 * Uses Folia's {@code GlobalRegionScheduler} to ensure thread-safe player access.
 */
public final class AdminNotifier {

    private final JavaPlugin plugin;

    public AdminNotifier(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void notifyAdmins(Component message) {
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(PluginPermission.ADMIN_UPDATES.node())) {
                    player.sendMessage(message);
                }
            }
        });
    }
}
