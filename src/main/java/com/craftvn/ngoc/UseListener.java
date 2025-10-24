package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    public UseListener(GemManager m){ this.manager = m; }

    @EventHandler
    public void onUse(PlayerInteractEvent e){
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack it = p.getInventory().getItemInMainHand();
        if (!isGem(it)) return;

        GemType g = getGemType(it);
        if (g == null) return;

        // Check expiration if exist
        ItemMeta meta = it.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Long expireAt = pdc.has(NgocPlugin.KEY_EXPIRE_AT, PersistentDataType.LONG) ?
                pdc.get(NgocPlugin.KEY_EXPIRE_AT, PersistentDataType.LONG) : null;
        if (expireAt != null && System.currentTimeMillis() >= expireAt){
            // remove this item
            it.setAmount(it.getAmount()-1);
            p.sendMessage(cc("&cNgọc đã hết hạn sử dụng!"));
            return;
        }

        // Activate via manager (manager handles cooldown & effects)
        manager.activate(p, g);

        // Consume on use
        if (g.consume()){
            it.setAmount(it.getAmount()-1);
        }
    }

    private static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }

    private boolean isGem(ItemStack it){
        if (it==null || !it.hasItemMeta()) return false;
        return it.getItemMeta().getPersistentDataContainer().has(NgocPlugin.KEY_GEM, PersistentDataType.STRING);
    }
    private GemType getGemType(ItemStack it){
        if (it==null || !it.hasItemMeta()) return null;
        PersistentDataContainer pdc = it.getItemMeta().getPersistentDataContainer();
        String s = pdc.get(NgocPlugin.KEY_GEM, PersistentDataType.STRING);
        if (s==null) return null;
        try{ return GemType.valueOf(s); } catch (Exception ex){ return null; }
    }
}
