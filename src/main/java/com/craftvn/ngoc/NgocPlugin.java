
package com.craftvn.ngoc;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class NgocPlugin extends JavaPlugin {
    private static NgocPlugin instance;
    private GemManager gemManager;
    public static NamespacedKey KEY_GEM;
    public static NamespacedKey KEY_EXPIRE_AT;

    @Override public void onEnable(){
        instance = this;
        KEY_GEM = new NamespacedKey(this, "gem_type");
        KEY_EXPIRE_AT = new NamespacedKey(this, "expire_at");

        this.gemManager = new GemManager(this);

        NgocCommand exec = new NgocCommand(gemManager);
        getCommand("muangoc").setExecutor(exec);
        getCommand("layngoc").setExecutor(exec);
        getCommand("layngoc").setTabCompleter(new NgocTabCompleter());

        getServer().getPluginManager().registerEvents(new PCGui(gemManager), this);
        getServer().getPluginManager().registerEvents(new UseListener(gemManager), this);
        getServer().getPluginManager().registerEvents(new FakeTotemListener(gemManager), this);
        getServer().getPluginManager().registerEvents(new CombatTracker(gemManager), this);

        getLogger().info("NgocPlugin da bat!");
    }
    @Override public void onDisable(){ getLogger().info("NgocPlugin da tat!"); }

    public static NgocPlugin get(){ return instance; }
    public GemManager manager(){ return gemManager; }
}
