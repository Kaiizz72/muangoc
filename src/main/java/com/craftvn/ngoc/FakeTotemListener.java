
package com.craftvn.ngoc;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FakeTotemListener implements Listener {
    private final GemManager manager;
    public FakeTotemListener(GemManager m){ this.manager = m; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLethal(EntityDamageEvent e){
        if (!(e.getEntity() instanceof Player p)) return;
        double finalHp = p.getHealth() - e.getFinalDamage();
        if (finalHp > 0) return;

        ItemStack main = p.getInventory().getItemInMainHand();
        ItemStack off = p.getInventory().getItemInOffHand();
        ItemStack it = isFakeTotem(main) ? main : (isFakeTotem(off) ? off : null);
        if (it==null) return;

        e.setCancelled(true);
        if (it==main) p.getInventory().setItemInMainHand(null); else p.getInventory().setItemInOffHand(null);

        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE,1,1);
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*5, 0));
        double base = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        p.setHealth(Math.min(base, 2.0));
    }

    private boolean isFakeTotem(ItemStack it){
        if (it==null || !it.hasItemMeta()) return false;
        ItemMeta m = it.getItemMeta();
        String type = m.getPersistentDataContainer().get(NgocPlugin.KEY_GEM, PersistentDataType.STRING);
        if (type==null) return false;
        try{ return GemType.valueOf(type)==GemType.TOTEM_GIA; }catch (Exception e){ return false; }
    }
}
