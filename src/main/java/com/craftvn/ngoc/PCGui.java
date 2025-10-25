package com.craftvn.ngoc;

import org.bukkit.entity.Player;

/**
 * PCGui (stub) – lớp vỏ bọc để các chỗ gọi PCGui vẫn compile/run được.
 * Bên trong forward sang PCGuiCompat (GUI fallback kiểu rương).
 */
public class PCGui {
    private final GemManager manager;

    public PCGui(GemManager manager) {
        this.manager = manager;
    }

    /** Instance APIs (nếu code khác tạo new PCGui(manager) rồi gọi) */
    public void openMainMenu(Player p) { PCGuiCompat.openMainForm(p, manager); }
    public void openMainForm(Player p) { PCGuiCompat.openMainForm(p, manager); }

    /** Static APIs (nếu code khác gọi thẳng static) */
    public static void openMainMenu(Player p, GemManager m) { PCGuiCompat.openMainForm(p, m); }
    public static void openMainForm(Player p, GemManager m) { PCGuiCompat.openMainForm(p, m); }
}
