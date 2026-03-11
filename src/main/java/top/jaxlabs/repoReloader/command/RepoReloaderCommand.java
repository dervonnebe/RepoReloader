package top.jaxlabs.repoReloader.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.jaxlabs.repoReloader.message.MessageFormatter;
import top.jaxlabs.repoReloader.message.MessageKey;
import top.jaxlabs.repoReloader.model.RepositoryEntry;
import top.jaxlabs.repoReloader.permission.PluginPermission;
import top.jaxlabs.repoReloader.update.UpdateChecker;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles the {@code /rr} command.
 *
 * <pre>
 *   /rr check              – force-checks all configured repositories immediately
 *   /rr check owner/repo   – force-checks a single repository
 * </pre>
 *
 * Each check runs asynchronously so it never blocks the main thread.
 * Tab-completion suggests known {@code owner/repo} keys.
 */
public final class RepoReloaderCommand implements CommandExecutor, TabCompleter {

    private static final String SUB_CHECK = "check";

    private final JavaPlugin plugin;
    private final UpdateChecker checker;
    private final List<RepositoryEntry> repositories;
    private final MessageFormatter messageFormatter;

    /**
     * @param checker      may be {@code null} when the plugin failed to initialise
     *                     (missing token / no repos). The command will report this gracefully.
     * @param repositories immutable list of configured repos, used for tab-completion and lookup
     */
    public RepoReloaderCommand(
            JavaPlugin plugin,
            UpdateChecker checker,
            List<RepositoryEntry> repositories,
            MessageFormatter messageFormatter
    ) {
        this.plugin = plugin;
        this.checker = checker;
        this.repositories = repositories;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PluginPermission.ADMIN_FORCE.node())) {
            sender.sendMessage(messageFormatter.format(MessageKey.CMD_NO_PERMISSION));
            return true;
        }

        if (checker == null) {
            sender.sendMessage(messageFormatter.format(MessageKey.CMD_NOT_INITIALIZED));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase(SUB_CHECK)) {
            sender.sendMessage(messageFormatter.format(MessageKey.CMD_USAGE));
            return true;
        }

        if (args.length == 1) {
            checkAll(sender);
        } else {
            checkOne(sender, args[1]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PluginPermission.ADMIN_FORCE.node())) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of(SUB_CHECK);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase(SUB_CHECK)) {
            String partial = args[1].toLowerCase();
            return repositories.stream()
                    .map(RepositoryEntry::key)
                    .filter(key -> key.toLowerCase().startsWith(partial))
                    .toList();
        }

        return List.of();
    }

    private void checkAll(CommandSender sender) {
        Component message = messageFormatter.format(
                MessageKey.CMD_CHECK_ALL,
                Map.of("count", String.valueOf(repositories.size()))
        );
        sender.sendMessage(message);

        for (RepositoryEntry entry : repositories) {
            runAsync(() -> checker.check(entry));
        }
    }

    private void checkOne(CommandSender sender, String repoKey) {
        Optional<RepositoryEntry> entryOpt = repositories.stream()
                .filter(e -> e.key().equalsIgnoreCase(repoKey))
                .findFirst();

        if (entryOpt.isEmpty()) {
            sender.sendMessage(messageFormatter.format(
                    MessageKey.CMD_REPO_NOT_FOUND,
                    Map.of("repo", repoKey)
            ));
            return;
        }

        sender.sendMessage(messageFormatter.format(
                MessageKey.CMD_CHECK_ONE,
                Map.of("repo", entryOpt.get().key())
        ));
        runAsync(() -> checker.check(entryOpt.get()));
    }

    private void runAsync(Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }
}
