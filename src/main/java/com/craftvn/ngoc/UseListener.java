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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UseListener implements Listener {
    private final GemManager manager;
    private final Map<UUID, Long> lastUse = new HashMap<>(); // chống double fire
    public UseListener(GemManager manager){ this.manager = manager; }

    @EventHandler(ignoreCancelled = true)
    public void onUse(PlayerInteractEvent e){
        Action a = e.getAction();
        boolean click = (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK ||
                         a == Action.LEFT_CLICK_AIR  || a == Action.LEFT_CLICK_BLOCK);
        if (!click) return;

        Player p = e.getPlayer();
        long now = System.currentTimeMillis();
        Long prev = lastUse.get(p.getUniqueId());
        if (prev != null && now - prev < 120) return; // chặn spam trong 120ms
        lastUse.put(p.getUniqueId(), now);

        // Ưu tiên tay phát sinh event; cho phép OFF_HAND
        EquipmentSlot hand = e.getHand();
        if (hand == null) hand = EquipmentSlot.HAND;

        ItemStack it = (hand == EquipmentSlot.OFF_HAND) ? p.getInventory().getItemInOffHand() : e.getItem();
        if (it == null || !it.hasItemMeta()) return;
        ItemMeta meta = it.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String s = pdc.get(NgocPlugin.KEY_GEM, PersistentDataType.STRING);
        if (s == null) return;

        GemType g;
        try{ g = GemType.valueOf(s); }catch (Exception ex){ return; }

        e.setCancelled(true);

        // kích hoạt
        manager.activate(p, g);

        // nếu là ngọc xài 1 lần -> trừ đúng 1 viên ở tay tương ứng
        if (g.consume()){
            int amount = it.getAmount();
            if (hand == EquipmentSlot.OFF_HAND){
                if (amount <= 1) p.getInventory().setItemInOffHand(null);
                else { it.setAmount(amount-1); p.getInventory().setItemInOffHand(it); }
            } else {
                if (amount <= 1) p.getInventory().setItemInMainHand(null);
                else { it.setAmount(amount-1); p.getInventory().setItemInMainHand(it); }
            }
        }
    }
}
