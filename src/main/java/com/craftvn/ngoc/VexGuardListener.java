package com.craftvn.ngoc;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class VexGuardListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent e){
        Entity ent = e.getEntity();
        if (!(ent instanceof Vex)) return;
        if (!"VEX_GUARD".equals(ent.getPersistentDataContainer().get(NgocPlugin.KEY_GEM, PersistentDataType.STRING))) return;
        String ownerId = ent.getPersistentDataContainer().get(new NamespacedKey(ent.getServer().getPluginManager().getPlugins()[0], "owner"), PersistentDataType.STRING);
        if (ownerId == null) return;
        if (e.getTarget() instanceof Player p && p.getUniqueId().toString().equals(ownerId)){
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFriendlyFire(EntityDamageByEntityEvent e){
        Entity damager = e.getDamager();
        if (!(damager instanceof Vex)) return;
        if (!"VEX_GUARD".equals(damager.getPersistentDataContainer().get(NgocPlugin.KEY_GEM, PersistentDataType.STRING))) return;
        String ownerId = damager.getPersistentDataContainer().get(new NamespacedKey(damager.getServer().getPluginManager().getPlugins()[0], "owner"), PersistentDataType.STRING);
        if (ownerId == null) return;
        if (e.getEntity() instanceof Player p && p.getUniqueId().toString().equals(ownerId)){
            e.setCancelled(true);
        }
    }
}
