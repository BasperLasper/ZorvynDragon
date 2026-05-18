package dev.zorvyndragon;

import org.bukkit.plugin.java.JavaPlugin;

public class ZorvynDragon extends JavaPlugin {

    private DragonManager dragonManager;
    private MessageUtil messages;

    @Override
    public void onEnable() {
        getLogger().info("ZorvynDragon enabled!");
        saveDefaultConfig();
        messages = new MessageUtil(this);
        dragonManager = new DragonManager(this);
        getServer().getPluginManager().registerEvents(new DragonListener(this), this);
        getCommand("zorvyndragon").setExecutor(new DragonCommand(this));
        getCommand("zorvyndragon").setTabCompleter(new DragonTabCompleter());
    }

    @Override
    public void onDisable() {
        getLogger().info("ZorvynDragon disabled.");
    }

    public DragonManager getDragonManager() { return dragonManager; }
    public MessageUtil getMessages() { return messages; }
}
