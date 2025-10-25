
package com.craftvn.ngoc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatTracker implements Listener {
    private final GemManager manager;
    public CombatTracker(GemManager m){ this.manager = m; }

    @EventHandler public void onHit(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player p){
            manager.markHit(p);
        }
    }
}
