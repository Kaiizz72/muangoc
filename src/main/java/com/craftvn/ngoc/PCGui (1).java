package com.craftvn.ngoc;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * PCGui (stub) – implements Listener để có thể register qua Bukkit.
 * Forward mọi thao tác mở GUI sang PCGuiCompat.
 */
public class PCGui implements Listener {
    private final GemManager manager;

    public PCGui(GemManager manager) {
        this.manager = manager;
    }

    /** Instance APIs */
    public void openMainMenu(Player p) { PCGuiCompat.openMainForm(p, manager); }
    public void openMainForm(Player p) { PCGuiCompat.openMainForm(p, manager); }

    /** Static APIs */
    public static void openMainMenu(Player p, GemManager m) { PCGuiCompat.openMainForm(p, m); }
    public static void openMainForm(Player p, GemManager m) { PCGuiCompat.openMainForm(p, m); }
}
