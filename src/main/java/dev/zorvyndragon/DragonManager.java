package dev.zorvyndragon;

import org.bukkit.*;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DragonManager {

    private final ZorvynDragon plugin;
    private boolean dragonActive = false;
    private BukkitTask pendingSpawnTask = null;
    private final Set<UUID> playersWaiting = new HashSet<>();

    public DragonManager(ZorvynDragon plugin) {
        this.plugin = plugin;
    }

    public boolean isDragonActive() { return dragonActive; }
    public void setDragonActive(boolean active) { this.dragonActive = active; }

    /** Called when a player enters the end for the first time this session */
    public void handlePlayerEnteredEnd(org.bukkit.entity.Player player) {
        if (dragonActive) {
            if (plugin.getConfig().getBoolean("messages.notify-dragon-active", true)) {
                player.sendMessage(plugin.getMessages().get("dragon-already-active"));
            }
            return;
        }

        playersWaiting.add(player.getUniqueId());

        int groupDelay = plugin.getConfig().getInt("spawn.group-wait-seconds", 5);

        if (pendingSpawnTask == null) {
            // First player — start countdown
            broadcastEnd(plugin.getMessages().get("dragon-spawning-soon",
                    "seconds", String.valueOf(groupDelay)));

            pendingSpawnTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                pendingSpawnTask = null;
                spawnDragon();
            }, groupDelay * 20L);
        } else {
            // Another player joined during countdown
            broadcastEnd(plugin.getMessages().get("player-joined-fight", "player", player.getName()));
        }
    }

    public void spawnDragon() {
        playersWaiting.clear();
        World end = getEndWorld();
        if (end == null) {
            plugin.getLogger().warning("Could not find the End world!");
            return;
        }

        DragonBattle battle = end.getEnderDragonBattle();
        if (battle == null) {
            plugin.getLogger().warning("No DragonBattle found in the End world!");
            return;
        }

        // Respawn crystals / towers if configured
        if (plugin.getConfig().getBoolean("spawn.respawn-crystals", true)) {
            battle.initiateRespawn();
        } else {
            // Spawn dragon directly without crystal ritual
            spawnDragonDirect(end, battle);
        }

        dragonActive = true;

        boolean broadcast = plugin.getConfig().getBoolean("broadcast.on-spawn", true);
        if (broadcast) {
            String scope = plugin.getConfig().getString("broadcast.scope", "server");
            String msg = plugin.getMessages().getRaw("dragon-spawned");
            if (scope.equalsIgnoreCase("end")) {
                broadcastEnd(plugin.getMessages().get("dragon-spawned"));
            } else {
                Bukkit.broadcast(plugin.getMessages().get("dragon-spawned"));
            }
        }
    }

    private void spawnDragonDirect(World end, DragonBattle battle) {
        // Remove any existing dragon first
        for (EnderDragon d : end.getEntitiesByClass(EnderDragon.class)) d.remove();
        Location spawnLoc = new Location(end, 0, 128, 0);
        EnderDragon dragon = (EnderDragon) end.spawnEntity(spawnLoc, EntityType.ENDER_DRAGON);
        dragon.setHealth(plugin.getConfig().getDouble("dragon.max-health", 200.0));
    }

    /** Called when the dragon is defeated */
    public void handleDragonDeath(EnderDragon dragon) {
        dragonActive = false;
        playersWaiting.clear();

        // Drop dragon egg if configured
        if (plugin.getConfig().getBoolean("dragon.drop-egg", true)) {
            World end = getEndWorld();
            if (end != null) {
                Location dropLoc = plugin.getConfig().getBoolean("dragon.egg-drop-at-exit-portal", true)
                        ? new Location(end, 0, 65, 0)
                        : dragon.getLocation();

                // Small delay so the portal generates first
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    end.dropItemNaturally(dropLoc, new org.bukkit.inventory.ItemStack(Material.DRAGON_EGG));
                }, plugin.getConfig().getLong("dragon.egg-drop-delay-ticks", 40L));
            }
        }

        // Broadcast death
        if (plugin.getConfig().getBoolean("broadcast.on-death", true)) {
            String scope = plugin.getConfig().getString("broadcast.scope", "server");
            if (scope.equalsIgnoreCase("end")) {
                broadcastEnd(plugin.getMessages().get("dragon-defeated"));
            } else {
                Bukkit.broadcast(plugin.getMessages().get("dragon-defeated"));
            }
        }

        // Rewards
        handleRewards(dragon.getLocation());

        // Schedule auto-reset if configured
        int autoResetMinutes = plugin.getConfig().getInt("spawn.auto-reset-minutes", 0);
        if (autoResetMinutes > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                World endWorld = getEndWorld();
                if (endWorld != null && !isDragonActive()) {
                    spawnDragon();
                }
            }, autoResetMinutes * 60L * 20L);
        }
    }

    private void handleRewards(Location loc) {
        if (!plugin.getConfig().getBoolean("rewards.enabled", false)) return;
        World end = getEndWorld();
        if (end == null) return;

        List<String> rewardItems = plugin.getConfig().getStringList("rewards.items");
        for (String itemName : rewardItems) {
            try {
                Material mat = Material.valueOf(itemName.toUpperCase());
                end.dropItemNaturally(loc, new org.bukkit.inventory.ItemStack(mat));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid reward item: " + itemName);
            }
        }
    }

    public World getEndWorld() {
        String worldName = plugin.getConfig().getString("world.end-world-name", "world_the_end");
        return Bukkit.getWorld(worldName);
    }

    public void broadcastEnd(net.kyori.adventure.text.Component msg) {
        World end = getEndWorld();
        if (end == null) return;
        for (org.bukkit.entity.Player p : end.getPlayers()) p.sendMessage(msg);
    }

    public void cancelPendingSpawn() {
        if (pendingSpawnTask != null) {
            pendingSpawnTask.cancel();
            pendingSpawnTask = null;
        }
        playersWaiting.clear();
    }
}
