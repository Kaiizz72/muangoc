
package com.craftvn.ngoc;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class GemManager {
    private final NgocPlugin plugin;
    private final Map<UUID, Map<GemType, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Long> lastHit = new HashMap<>();

    public GemManager(NgocPlugin plugin){ this.plugin = plugin; }

    public void markHit(Player p){ lastHit.put(p.getUniqueId(), System.currentTimeMillis()); }
    public boolean recentlyHit(Player p, long ms){
        Long t = lastHit.get(p.getUniqueId());
        return t != null && System.currentTimeMillis() - t < ms;
    }

    public boolean canUse(Player p, GemType g){
        cooldowns.putIfAbsent(p.getUniqueId(), new HashMap<>());
        long now = System.currentTimeMillis();
        long cd = cooldowns.get(p.getUniqueId()).getOrDefault(g, 0L);
        if (now < cd){
            long left = Math.max(1, (cd - now)/1000);
            p.sendMessage(cc("&cNgọc đang hồi (&f"+left+"s&c)."));
            return false;
        }
        cooldowns.get(p.getUniqueId()).put(g, now + g.cooldown()*1000L);
        return true;
    }

    public ItemStack createGemItem(GemType gem){
        ItemStack it = new ItemStack(gem==GemType.TOTEM_GIA ? Material.TOTEM_OF_UNDYING : Material.SLIME_BALL);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(cc(gem.display()));
        List<String> lore = new ArrayList<>();
        lore.add(cc("&7➛ &f"+gem.desc()));
        lore.add(cc("&eYêu cầu: &f"+gem.exp()+" exp"+(gem.req()==null?"":" &7+ &f"+gem.amount()+" "+prettyName(gem.req()))));
        lore.add(cc("&7Cooldown: &f"+gem.cooldown()+"s"+(gem.expireMinutes()>0?" &7| Hạn: &f"+gem.expireMinutes()+"p": "")));
        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        m.getPersistentDataContainer().set(NgocPlugin.KEY_GEM, PersistentDataType.STRING, gem.name());
        it.setItemMeta(m);
        return it;
    }

    public void giveGem(Player p, GemType g){ p.getInventory().addItem(createGemItem(g)); }

    public void activate(Player p, GemType g){
        if (!canUse(p, g)) return;

        switch (g){
            case TANG_MAU -> {
                // +2 hàng máu (40 HP) trong 2 phút, cooldown 30s; ngọc hết hạn sau 20p (đã set khi tạo item)
                AttributeInstance a = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (a != null) {
                    double old = a.getBaseValue();
                    double target = old + 40.0;
                    a.setBaseValue(target);
                    p.setHealth(Math.min(p.getHealth() + 40.0, target));
                    new BukkitRunnable(){ @Override public void run(){
                        AttributeInstance ar = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        if (ar != null) {
                            ar.setBaseValue(old);
                            if (p.getHealth() > old) p.setHealth(old);
                        }
                        p.sendMessage(cc("&eHiệu ứng &cTăng Máu&f đã kết thúc."));
                    }}.runTaskLater(plugin, 20L*120);
                    fx(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, Particle.HEART);
                } else {
                    p.sendMessage(cc("&cKhông thể tăng máu (thiếu Attribute MAX_HEALTH)!"));
                }
            }

            case SINH_LUC -> { // +3 tim + regen II
                p.setHealth(Math.min(p.getHealth()+6.0, Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue()));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*10, 1));
                fx(p, Sound.ENTITY_PLAYER_LEVELUP, Particle.HEART);
            }
            case BAO_VE -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20*10, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*20, 1));
                fx(p, Sound.BLOCK_BEACON_ACTIVATE, Particle.INSTANT_EFFECT);
            }
            case HOI_PHUC -> {
                AttributeInstance a = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (a != null) p.setHealth(a.getBaseValue());
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*15, 0));
                fx(p, Sound.ENTITY_PLAYER_LEVELUP, Particle.HEART);
            }
            case SINH_MENH -> {
                AttributeInstance a = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (a != null){
                    a.setBaseValue(40.0);
                    p.setHealth(40.0);
                    new BukkitRunnable(){
                        @Override public void run(){
                            AttributeInstance ax = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (ax != null){
                                ax.setBaseValue(20.0);
                                if (p.getHealth()>20.0) p.setHealth(20.0);
                                p.sendMessage(cc("&cNgọc Sinh Mệnh đã hết hiệu lực."));
                            }
                        }
                    }.runTaskLater(plugin, 20L* (g.expireMinutes()>0? g.expireMinutes()*60L : 600L));
                    fx(p, Sound.ITEM_TOTEM_USE, Particle.HEART);
                }
            }
            case CUONG_NO -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20*15, 1));
                new BukkitRunnable(){ @Override public void run(){ p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20*10, 0)); } }.runTaskLater(plugin, 20*15);
                fx(p, Sound.ENTITY_WITHER_SPAWN, Particle.CRIT);
            }
            case HAC_AM -> {
                for (Player t : nearbyPlayers(p,5)){
                    t.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20*5, 1));
                    t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*3, 0));
                }
                fx(p, Sound.ENTITY_WITHER_SHOOT, Particle.LARGE_SMOKE);
            }
            case TRI_TRE -> {
                for (Player t : nearbyPlayers(p,7)){
                    t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20*5, 1));
                    t.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20*5, 0));
                }
                fx(p, Sound.BLOCK_HONEY_BLOCK_SLIDE, Particle.ITEM_SLIME);
            }
            case HO_VE -> {
                Player target = nearestPlayer(p, 10);
                if (target==null){ p.sendMessage(cc("&cKhông có mục tiêu gần!")); break; }
                for (int i=0;i<3;i++){
                    Vex v = (Vex) p.getWorld().spawnEntity(p.getLocation().add(0,1,0), EntityType.VEX);
                    v.setCustomName(cc("&cHộ vệ của "+p.getName()));
                    v.setCustomNameVisible(true);
                    v.setTarget(target);
                    v.setLifeTicks(20*20);
                }
                fx(p, Sound.ENTITY_VEX_CHARGE, Particle.SOUL);
            }
            case TOC_DO -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*20, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20*10, 0));
                fx(p, Sound.ENTITY_ARROW_SHOOT, Particle.CLOUD);
            }
            case NHAY_CAO -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20*20, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*20, 0));
                fx(p, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, Particle.CLOUD);
            }
            case LONG_KINH -> {
                makeBarrier(p.getLocation(), 3, 8);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, 1);
            }
            case SAM_SET -> {
                Player t = nearestPlayer(p, 10);
                if (t != null) p.getWorld().strikeLightning(t.getLocation());
                fx(p, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Particle.ELECTRIC_SPARK);
            }
            case DICH_CHUYEN -> {
                if (recentlyHit(p, 5000)){ p.sendMessage(cc("&cBạn đang bị đánh, không thể dịch chuyển!")); break; }
                Location loc = p.getLocation().clone();
                double dx = (Math.random()-0.5)*8; double dz = (Math.random()-0.5)*8;
                loc.add(dx, 0, dz);
                loc.setY(p.getWorld().getHighestBlockYAt(loc)+1);
                p.teleport(loc);
                fx(p, Sound.ENTITY_ENDERMAN_TELEPORT, Particle.PORTAL);
            }
            case CAM_BAY -> {
                Collection<Player> around = nearbyPlayers(p,7);
                List<Block> webs = new ArrayList<>();
                for (Player t: around){
                    Block b = t.getLocation().getBlock();
                    if (b.getType()==Material.AIR){ b.setType(Material.COBWEB,false); webs.add(b); }
                }
                Player target = nearestPlayer(p,7);
                new BukkitRunnable(){ @Override public void run(){
                    if (target!=null && target.isOnline()) p.teleport(target.getLocation());
                    for(Block b:webs) if (b.getType()==Material.COBWEB) b.setType(Material.AIR,false);
                    fx(p, Sound.ENTITY_ENDERMAN_TELEPORT, Particle.PORTAL);
                }}.runTaskLater(plugin, 40L);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_COBWEB_PLACE,1,1);
            }
            case TRUY_PHONG -> {
                Player target = p.getWorld().getNearbyPlayers(p.getLocation(),30).stream().filter(x->!x.equals(p)&&x.isGliding())
                        .min(Comparator.comparingDouble(x->x.getLocation().distance(p.getLocation()))).orElse(null);
                if (target==null){ p.sendMessage(cc("&cKhông có mục tiêu đang dùng Elytra!")); break; }
                Vector v = p.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.5);
                target.setVelocity(v);
                fx(p, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, Particle.CLOUD);
            }
            case TOTEM_GIA -> {
                p.sendMessage(cc("&7Giữ Totem Dởm trên tay để kích hoạt khi chết."));
                p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE,1,1);
            }
        }
    }

    public static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
    public static String prettyName(Material m){ return m.name().toLowerCase().replace('_',' '); }

    public static void fx(Player p, Sound s, Particle part){
        p.getWorld().playSound(p.getLocation(), s, 1f, 1f);
        p.getWorld().spawnParticle(part, p.getLocation(), 30, 1,1,1);
    }

    public static Player nearestPlayer(Player p, double r){
        return p.getWorld().getNearbyPlayers(p.getLocation(), r).stream().filter(x->!x.equals(p))
                .min(Comparator.comparingDouble(a->a.getLocation().distance(p.getLocation()))).orElse(null);
    }
    public static Collection<Player> nearbyPlayers(Player p, double r){
        return p.getWorld().getNearbyPlayers(p.getLocation(), r).stream().filter(x->!x.equals(p)).toList();
    }

    private void makeBarrier(Location c, int r, int seconds){
        List<Block> placed = new ArrayList<>();
        for (int x=-r;x<=r;x++) for (int y=0;y<=r;y++) for (int z=-r;z<=r;z++){
            if (Math.abs(x)==r || Math.abs(z)==r || y==r){
                Block b = c.getWorld().getBlockAt(c.clone().add(x,y,z));
                if (b.getType()==Material.AIR){ b.setType(Material.BARRIER,false); placed.add(b); }
            }
        }
        new BukkitRunnable(){ @Override public void run(){ for(Block b: placed) if (b.getType()==Material.BARRIER) b.setType(Material.AIR,false);} }.runTaskLater(plugin, 20L*seconds);
    }
}
