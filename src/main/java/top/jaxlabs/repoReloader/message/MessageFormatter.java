package top.jaxlabs.repoReloader.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

/**
 * Resolves a {@link MessageKey} against the plugin config, fills in placeholders,
 * and deserializes the result as a MiniMessage {@link Component}.
 */
public final class MessageFormatter {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final FileConfiguration config;

    public MessageFormatter(FileConfiguration config) {
        this.config = config;
    }

    public Component format(MessageKey key, Map<String, String> placeholders) {
        String template = config.getString(key.configPath(), key.defaultTemplate());
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return MINI.deserialize(template);
    }

    public Component format(MessageKey key) {
        return format(key, Map.of());
    }
}
