package dev.zorvyndragon;

import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class DragonTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("spawn", "kill", "reset", "status", "reload");
        return List.of();
    }
}
