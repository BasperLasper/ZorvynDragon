package dev.zorvyndragon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

public class DragonCommand implements CommandExecutor {

    private final ZorvynDragon plugin;

    public DragonCommand(ZorvynDragon plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        MessageUtil msg = plugin.getMessages();

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {

            // Force spawn dragon
            case "spawn" -> {
                if (!sender.hasPermission("zorvyndragon.spawn")) {
                    sender.sendMessage(msg.get("no-permission")); return true;
                }
                if (plugin.getDragonManager().isDragonActive()) {
                    sender.sendMessage(msg.get("dragon-already-active")); return true;
                }
                plugin.getDragonManager().cancelPendingSpawn();
                plugin.getDragonManager().spawnDragon();
                sender.sendMessage(msg.get("spawn-forced"));
            }

            // Force kill dragon
            case "kill" -> {
                if (!sender.hasPermission("zorvyndragon.kill")) {
                    sender.sendMessage(msg.get("no-permission")); return true;
                }
                World end = plugin.getDragonManager().getEndWorld();
                if (end == null) { sender.sendMessage(msg.get("end-world-not-found")); return true; }
                int killed = 0;
                for (EnderDragon d : end.getEntitiesByClass(EnderDragon.class)) {
                    d.remove();
                    killed++;
                }
                plugin.getDragonManager().setDragonActive(false);
                plugin.getDragonManager().cancelPendingSpawn();
                sender.sendMessage(msg.get("dragon-killed-admin", "count", String.valueOf(killed)));
            }

            // Reset — kill dragon, clear pending, allow re-trigger
            case "reset" -> {
                if (!sender.hasPermission("zorvyndragon.reset")) {
                    sender.sendMessage(msg.get("no-permission")); return true;
                }
                World end = plugin.getDragonManager().getEndWorld();
                if (end != null) {
                    for (EnderDragon d : end.getEntitiesByClass(EnderDragon.class)) d.remove();
                }
                plugin.getDragonManager().setDragonActive(false);
                plugin.getDragonManager().cancelPendingSpawn();
                sender.sendMessage(msg.get("dragon-reset"));
            }

            // Status
            case "status" -> {
                if (!sender.hasPermission("zorvyndragon.status")) {
                    sender.sendMessage(msg.get("no-permission")); return true;
                }
                boolean active = plugin.getDragonManager().isDragonActive();
                sender.sendMessage(msg.get(active ? "status-active" : "status-inactive"));
            }

            // Reload config
            case "reload" -> {
                if (!sender.hasPermission("zorvyndragon.reload")) {
                    sender.sendMessage(msg.get("no-permission")); return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(msg.get("config-reloaded"));
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("── ZorvynDragon ──", NamedTextColor.DARK_PURPLE));
        sender.sendMessage(Component.text("/zd spawn", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" — Force spawn the dragon", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/zd kill", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" — Remove the dragon", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/zd reset", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" — Reset dragon state", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/zd status", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" — Check dragon status", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/zd reload", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" — Reload config", NamedTextColor.GRAY)));
    }
}
