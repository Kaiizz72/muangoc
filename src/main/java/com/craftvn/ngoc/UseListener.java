package com.craftvn.ngoc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class UseListener implements Listener {
    private final GemManager manager;
    public UseListener(GemManager manager){ this.manager = manager; }

    @EventHandler(ignoreCancelled = true)
    public void onUse(PlayerInteractEvent e){
        if (e.getHand() != EquipmentSlot.HAND) return; // chỉ tay chính để tránh gọi đôi
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta()) return;
        ItemMeta meta = it.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String s = pdc.get(NgocPlugin.KEY_GEM, PersistentDataType.STRING);
        if (s == null) return;

        GemType g;
        try{ g = GemType.valueOf(s); }catch (Exception ex){ return; }

        e.setCancelled(true);
        Player p = e.getPlayer();

        // kích hoạt
        manager.activate(p, g);

        // nếu là ngọc xài 1 lần -> trừ đúng 1 viên ở tay chính
        if (g.consume()){
            int amount = it.getAmount();
            if (amount <= 1) p.getInventory().setItemInMainHand(null);
            else { it.setAmount(amount-1); p.getInventory().setItemInMainHand(it); }
        }
    }
}
