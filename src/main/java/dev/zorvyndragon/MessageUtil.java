package dev.zorvyndragon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageUtil {

    private final ZorvynDragon plugin;

    public MessageUtil(ZorvynDragon plugin) { this.plugin = plugin; }

    public Component get(String key, String... replacements) {
        String raw = getRaw(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    public String getRaw(String key) {
        return plugin.getConfig().getString("messages." + key, "&cMissing message: " + key);
    }
}
