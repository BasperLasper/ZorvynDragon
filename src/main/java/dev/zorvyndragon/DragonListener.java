package dev.zorvyndragon;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.entity.EntityDropItemEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DragonListener implements Listener {

    private final ZorvynDragon plugin;
    // Track players who have already triggered the dragon this session
    private final Set<UUID> triggeredThisSession = new HashSet<>();

    public DragonListener(ZorvynDragon plugin) { this.plugin = plugin; }

    /** Player enters the End */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerEnterEnd(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World newWorld = player.getWorld();

        if (newWorld.getEnvironment() != World.Environment.THE_END) return;
        if (!plugin.getConfig().getBoolean("spawn.trigger-on-enter", true)) return;

        // Only trigger once per player per session (or always, based on config)
        boolean onlyFirstTime = plugin.getConfig().getBoolean("spawn.only-first-entry-per-session", true);
        if (onlyFirstTime && triggeredThisSession.contains(player.getUniqueId())) return;

        triggeredThisSession.add(player.getUniqueId());
        plugin.getDragonManager().handlePlayerEnteredEnd(player);
    }

    /** Player leaves the End — remove from session tracking if configured */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeaveEnd(PlayerChangedWorldEvent event) {
        World from = event.getFrom();
        if (from.getEnvironment() != World.Environment.THE_END) return;

        boolean resetOnLeave = plugin.getConfig().getBoolean("spawn.reset-trigger-on-leave", true);
        if (resetOnLeave) {
            triggeredThisSession.remove(event.getPlayer().getUniqueId());
        }
    }

    /** Dragon dies */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        // Remove vanilla dragon egg drop — we handle it ourselves
        if (plugin.getConfig().getBoolean("dragon.drop-egg", true)) {
            event.getDrops().removeIf(item -> item.getType() == org.bukkit.Material.DRAGON_EGG);
        }

        // Remove vanilla XP drop if configured
        if (!plugin.getConfig().getBoolean("dragon.drop-vanilla-xp", true)) {
            event.setDroppedExp(0);
        } else {
            int customXp = plugin.getConfig().getInt("dragon.custom-xp", -1);
            if (customXp >= 0) event.setDroppedExp(customXp);
        }

        plugin.getDragonManager().handleDragonDeath(dragon);
    }
}
