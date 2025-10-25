
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

    @EventHandler public void use(PlayerInteractEvent e){
        if (e.getAction()!= Action.RIGHT_CLICK_AIR && e.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        ItemStack off = p.getInventory().getItemInOffHand();
        ItemStack it = null; EquipmentSlot hand = null;
        if (isGem(main)){ it=main; hand=EquipmentSlot.HAND; }
        else if (isGem(off)){ it=off; hand=EquipmentSlot.OFF_HAND; }
        else return;

        GemType g = getGemType(it);
        if (g==null) return;

        manager.activate(p, g);

        if (g.consume()){
            int amt = it.getAmount();
            if (amt<=1){
                if (hand==EquipmentSlot.HAND) p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                else p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            } else it.setAmount(amt-1);
        }
    }

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
