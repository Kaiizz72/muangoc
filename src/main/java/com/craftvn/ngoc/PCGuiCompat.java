package com.craftvn.ngoc;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class PCGuiCompat {
    /** Fallback GUI đơn giản: mở một rương gồm tất cả các ngọc để người chơi xem thông tin. */
    public static void openMainForm(Player p, GemManager manager){
        int size = 27;
        Inventory inv = Bukkit.createInventory(p, size, GemManager.cc("&0&lMUA NGỌC &7(PC - Compat)"));
        int i = 0;
        for (GemType g : GemType.values()){
            if (i >= size) break;
            ItemStack it = manager.createGemItem(g);
            inv.setItem(i++, it);
        }
        p.openInventory(inv);
    }
}
