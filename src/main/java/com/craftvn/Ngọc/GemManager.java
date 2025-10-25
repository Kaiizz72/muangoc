
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
    private final Map<UUID, Map<GemType, Long>> activeUntil = new HashMap<>();
private final Map<UUID, org.bukkit.scheduler.BukkitTask> maxHealthBuffTask = new HashMap<>();
private final Map<UUID, Double> maxHealthOldBase = new HashMap<>();

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
    if (isActive(p, g)) {
        p.sendMessage(cc("&cBạn đang có hiệu ứng của " + g.display() + "&c. Còn &f" + activeLeftSec(p, g) + "s&c."));
        return false;
    }
    if (g.consume()) return true;
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
cooldowns.get(p.getUniqueId()).put(g, now + g.cooldown()*1000L);
        return true;
    }

    public ItemStack createGemItem(GemType gem){
    ItemStack it = new ItemStack(gem==GemType.TOTEM_GIA ? Material.TOTEM_OF_UNDYING : Material.SLIME_BALL);
    ItemMeta m = it.getItemMeta();
    m.setDisplayName(cc(gem.display()));
    java.util.List<String> lore = new java.util.ArrayList<>();
    lore.add(small("➛ " + gem.desc()));
    String cd = gem.consume() ? "0s (xài 1 lần)" : (gem.cooldown()+"s");
    lore.add(small("cooldown: " + cd + (gem.expireMinutes()>0 ? " | hạn: " + gem.expireMinutes() + "p" : "")));
    m.setLore(lore);
    m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
    m.getPersistentDataContainer().set(NgocPlugin.KEY_GEM, PersistentDataType.STRING, gem.name());
    it.setItemMeta(m);
    return it;
}
public void giveGem(Player p, GemType g){ p.getInventory().addItem(createGemItem(g)); }

    public void activate(Player p, GemType g){
        if (!canUse(p, g)) return;

        switch (g){
            case SINH_LUC -> { // +3 tim + regen II
                p.setHealth(Math.min(p.getHealth()+6.0, Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue()));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*10, 1));
                fx(p, Sound.ENTITY_PLAYER_LEVELUP, Particle.HEART);
            }
            case BAO_VE -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*10, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*20, 1));
                fx(p, Sound.BLOCK_BEACON_ACTIVATE, Particle.SPELL_INSTANT);
            }
            case HOI_PHUC -> {
                AttributeInstance a = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (a != null) p.setHealth(a.getBaseValue());
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*15, 0));
                fx(p, Sound.ENTITY_PLAYER_LEVELUP, Particle.HEART);
            }
            case SINH_MENH -> {
        applyMaxHealthBuff(p, 40.0, 20L * 60 * 5, "&cNgọc Sinh Mệnh đã hết hiệu lực.");
        markActive(p, GemType.SINH_MENH, 20L * 60 * 5);
        fx(p, Sound.ITEM_TOTEM_USE, Particle.HEART);
    }
}
                    }.runTaskLater(plugin, 20L* (g.expireMinutes()>0? g.expireMinutes()*60L : 600L));
                    fx(p, Sound.ITEM_TOTEM_USE, Particle.HEART);
                }
            }
            case CUONG_NO -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20*15, 1));
                new BukkitRunnable(){ @Override public void run(){ p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20*10, 0)); } }.runTaskLater(plugin, 20*15);
                fx(p, Sound.ENTITY_WITHER_SPAWN, Particle.CRIT);
            }
            case HAC_AM -> {
                for (Player t : nearbyPlayers(p,5)){
                    t.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20*5, 1));
                    t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*3, 0));
                }
                fx(p, Sound.ENTITY_WITHER_SHOOT, Particle.SMOKE);
            }
            case TRI_TRE -> {
                for (Player t : nearbyPlayers(p,7)){
                    t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*5, 1));
                    t.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20*5, 0));
                }
                fx(p, Sound.BLOCK_HONEY_BLOCK_SLIDE, Particle.SLIME);
            }
            case HO_VE -> {
                Player target = nearestPlayer(p, 10);
                if (target==null){ p.sendMessage(cc("&cKhông có mục tiêu gần!")); break; }
                for (int i=0;i<3;i++){
                    Vex v = (Vex) p.getWorld().spawnEntity(p.getLocation().add(0,1,0), EntityType.VEX);
                    v.setCustomName(cc("&cHộ vệ của "+p.getName()));
                    v.setCustomNameVisible(true);
                    v.setTarget(target);
                    v.setLimitedLife(20*20);
                }
                fx(p, Sound.ENTITY_VEX_CHARGE, Particle.SOUL);
            }
            case TOC_DO -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*20, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20*10, 0));
                fx(p, Sound.ENTITY_ARROW_SHOOT, Particle.CLOUD);
            }
            case NHAY_CAO -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20*20, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*20, 0));
                fx(p, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, Particle.CLOUD);
            }
            case LONG_KINH -> {
        makeGlassCageSmart(p.getLocation(), 4, 5, 10);
        markActive(p, GemType.LONG_KINH, 20L * 10);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, 1);
    }
case SAM_SET -> {
                Player t = nearestPlayer(p, 10);
                if (t != null) p.getWorld().strikeLightning(t.getLocation());
                fx(p, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Particle.ELECTRIC_SPARK);
            }
            case DICH_CHUYEN -> {
        if (recentlyHit(p, 5000)){ p.sendMessage(cc("&cBạn đang bị đánh, không thể dịch chuyển!")); break; }
        Location dest = findSafeRandomLocationNear(p, 7, 10, 24);
        if (dest == null) { p.sendMessage(cc("&cKhông tìm được vị trí an toàn, hãy thử lại!")); break; }
        p.teleport(dest);
        markActive(p, GemType.DICH_CHUYEN, 20L * 1);
        fx(p, Sound.ENTITY_ENDERMAN_TELEPORT, Particle.PORTAL);
    }
Location loc = p.getLocation().clone();
                double dx = (Math.random()-0.5)*8; double dz = (Math.random()-0.5)*8;
                loc.add(dx, 0, dz);
                loc.setY(p.getWorld().getHighestBlockYAt(loc)+1);
                p.teleport(loc);
                fx(p, Sound.ENTITY_ENDERMAN_TELEPORT, Particle.PORTAL);
            }
            case CAM_BAY -> {
        Player target = nearestPlayer(p, 7);
        if (target == null) { p.sendMessage(cc("&cKhông có mục tiêu trong 7 block!")); break; }
        try { if (target.isGliding()) target.setGliding(false); } catch (Throwable ignored) {}
        boolean airborne = target.getLocation().clone().subtract(0,1,0).getBlock().getType().isAir();
        if (airborne) {
            spawnGlassPadUnder(target.getLocation(), 1, 6, 6);
        }
        java.util.List<Block> webs = placeCobwebOn(target, 1);
        Location dest = target.getLocation().clone().add(0.5, 0, 0.5);
        p.teleport(dest);
        fx(p, Sound.ENTITY_ENDERMAN_TELEPORT, Particle.PORTAL);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 3, 0, true, false, false));
        new BukkitRunnable(){ @Override public void run(){
            for (Block b : webs) if (b.getType() == Material.COBWEB) b.setType(Material.AIR, false);
        }}.runTaskLater(plugin, 20L * 4);
        markActive(p, GemType.CAM_BAY, 20L * 2);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_COBWEB_PLACE, 1, 1);
    }
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
        Player target = p.getWorld().getNearbyPlayers(p.getLocation(), 40).stream()
                .filter(x -> !x.equals(p) && (x.isGliding() || wearingElytra(x)))
                .min(Comparator.comparingDouble(x -> x.getLocation().distance(p.getLocation())))
                .orElse(null);
        if (target == null) { p.sendMessage(cc("&cKhông tìm thấy ai đang dùng/mặc Elytra trong 40 block!")); break; }
        pullPlayerTo(target, p.getLocation().clone().add(0, 0.5, 0), 1.2, 20);
        markActive(p, GemType.TRUY_PHONG, 20L * 2);
        fx(p, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, Particle.CLOUD);
    }
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


private static String small(String raw) {
    return cc("&8&o" + org.bukkit.ChatColor.stripColor(cc(raw)));
}



private boolean isActive(Player p, GemType g) {
    var m = activeUntil.get(p.getUniqueId());
    if (m == null) return false;
    Long until = m.get(g);
    return until != null && until > System.currentTimeMillis();
}
private long activeLeftSec(Player p, GemType g){
    var m = activeUntil.get(p.getUniqueId());
    if (m == null) return 0;
    Long until = m.get(g);
    if (until == null) return 0;
    long left = (until - System.currentTimeMillis())/1000;
    return Math.max(0, left);
}
private void markActive(Player p, GemType g, long durationTicks){
    activeUntil.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
            .put(g, System.currentTimeMillis() + durationTicks * 50L);
}



private void applyMaxHealthBuff(Player p, double extraHealth, long durationTicks, String endMsg){
    AttributeInstance a = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
    if (a == null) {
        p.sendMessage(cc("&cKhông thể tăng máu (thiếu Attribute MAX_HEALTH)!"));
        return;
    }
    var oldTask = maxHealthBuffTask.remove(p.getUniqueId());
    if (oldTask != null) oldTask.cancel();
    double oldBase = a.getBaseValue();
    maxHealthOldBase.put(p.getUniqueId(), oldBase);
    double target = oldBase + extraHealth;
    a.setBaseValue(target);
    p.setHealth(Math.min(p.getHealth() + extraHealth, target));
    var task = new BukkitRunnable(){
        @Override public void run(){
            AttributeInstance ar = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            Double old = maxHealthOldBase.remove(p.getUniqueId());
            if (ar != null && old != null){
                ar.setBaseValue(old);
                if (p.getHealth() > old) p.setHealth(old);
            }
            maxHealthBuffTask.remove(p.getUniqueId());
            if (endMsg != null && !endMsg.isEmpty()){
                p.sendMessage(cc(endMsg));
            }
        }
    }.runTaskLater(plugin, durationTicks);
    maxHealthBuffTask.put(p.getUniqueId(), task);
}



private boolean wearingElytra(Player x) {
    ItemStack chest = x.getInventory().getChestplate();
    return chest != null && chest.getType() == Material.ELYTRA;
}
private void pullPlayerTo(Player target, Location to, double baseSpeed, int ticks) {
    new BukkitRunnable() {
        int i = 0;
        @Override public void run() {
            if (!target.isOnline() || !target.getWorld().equals(to.getWorld())) { cancel(); return; }
            double dist = target.getLocation().distance(to);
            if (dist < 1.5 || i++ >= ticks) { cancel(); return; }
            Vector dir = to.toVector().subtract(target.getLocation().toVector()).normalize();
            double speed = Math.max(baseSpeed, Math.min(1.5, dist / 4.0));
            Vector push = dir.multiply(speed);
            Vector vel = target.getVelocity().multiply(0.25).add(push.multiply(0.85));
            vel.setY(Math.max(vel.getY(), 0.25 + (to.getY() - target.getLocation().getY()) * 0.05));
            try { target.setGliding(false); } catch (Throwable ignored) {}
            target.setVelocity(vel);
            target.setFallDistance(0f);
            target.playSound(target.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 0.6f, 1.2f);
        }
    }.runTaskTimer(plugin, 0L, 1L);
}



private Location findSafeRandomLocationNear(Player p, int minRange, int maxRange, int tries){
    var w = p.getWorld();
    java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();
    for (int i = 0; i < tries; i++){
        double angle = rnd.nextDouble() * Math.PI * 2;
        double dist = minRange + rnd.nextDouble() * (maxRange - minRange);
        int x = (int)Math.floor(p.getLocation().getX() + Math.cos(angle) * dist);
        int z = (int)Math.floor(p.getLocation().getZ() + Math.sin(angle) * dist);
        int y = w.getHighestBlockYAt(x, z);
        if (y <= w.getMinHeight()) continue;
        Block floor = w.getBlockAt(x, y - 1, z);
        if (!isGoodGround(floor.getType())) continue;
        Block b1 = w.getBlockAt(x, y, z);
        Block b2 = w.getBlockAt(x, y + 1, z);
        if (!b1.getType().isAir() || !b2.getType().isAir()) continue;
        return new Location(w, x + 0.5, y, z + 0.5);
    }
    return null;
}
private boolean isGoodGround(Material m){
    if (m.isAir()) return false;
    if (!m.isSolid()) return false;
    if (m == Material.CACTUS || m == Material.MAGMA_BLOCK ||
        m == Material.CAMPFIRE || m == Material.SOUL_CAMPFIRE) return false;
    String n = m.name();
    if (n.contains("WATER") || n.contains("LAVA")) return false;
    return true;
}



private void makeGlassCageSmart(Location around, int radius, int height, int seconds){
    List<Block> placed = new ArrayList<>();
    var w = around.getWorld();
    int bx = around.getBlockX();
    int bz = around.getBlockZ();
    int baseY;
    Block feet = w.getBlockAt(bx, around.getBlockY(), bz);
    if (feet.getType().isAir()) baseY = w.getHighestBlockYAt(bx, bz);
    else baseY = feet.getY();
    baseY = Math.min(Math.max(baseY, w.getMinHeight() + 1), w.getMaxHeight() - height - 2);
    Location base = new Location(w, bx + 0.5, baseY + 1, bz + 0.5);
    for (int x=-radius+1;x<=radius-1;x++)
        for (int y=0;y<height;y++)
            for (int z=-radius+1;z<=radius-1;z++){
                Block b = w.getBlockAt(base.clone().add(x,y,z));
                if (!b.getType().isAir()) b.setType(Material.AIR,false);
            }
    for (int x=-radius;x<=radius;x++)
        for (int z=-radius;z<=radius;z++){
            Block floor = w.getBlockAt(base.clone().add(x,0,z));
            if (floor.getType()==Material.AIR){ floor.setType(Material.GLASS,false); placed.add(floor); }
            Block roof = w.getBlockAt(base.clone().add(x,height,z));
            if (roof.getType()==Material.AIR){ roof.setType(Material.GLASS,false); placed.add(roof); }
        }
    for (int y=0;y<=height;y++)
        for (int x=-radius;x<=radius;x++)
            for (int z=-radius;z<=radius;z++){
                boolean wall = (Math.abs(x)==radius || Math.abs(z)==radius);
                if (!wall) continue;
                Block b = w.getBlockAt(base.clone().add(x,y,z));
                if (b.getType()==Material.AIR){ b.setType(Material.GLASS,false); placed.add(b); }
            }
    new BukkitRunnable(){ @Override public void run(){
        for(Block b: placed) if (b.getType()==Material.GLASS) b.setType(Material.AIR,false);
    }}.runTaskLater(plugin, 20L*seconds);
}



private List<Block> placeCobwebOn(Player t, int radius) {
    List<Block> placed = new ArrayList<>();
    var w = t.getWorld();
    Location l = t.getLocation();
    int bx = l.getBlockX(), by = l.getBlockY(), bz = l.getBlockZ();
    int[][] offsets = { {0,0}, {1,0}, {-1,0}, {0,1}, {0,-1} };
    for (int[] off : offsets){
        for (int dy = 0; dy <= 1; dy++){
            Block b = w.getBlockAt(bx + off[0], by + dy, bz + off[1]);
            if (b.getType().isAir()) { b.setType(Material.COBWEB, false); placed.add(b); }
        }
    }
    if (radius >= 1){
        for (int x = -radius; x <= radius; x++){
            for (int z = -radius; z <= radius; z++){
                if (Math.abs(x) + Math.abs(z) != 2) continue;
                for (int dy = 0; dy <= 1; dy++){
                    Block b = w.getBlockAt(bx + x, by + dy, bz + z);
                    if (b.getType().isAir()) { b.setType(Material.COBWEB, false); placed.add(b); }
                }
            }
        }
    }
    return placed;
}



private List<Block> spawnGlassPadUnder(Location center, int halfSize, int maxDown, int seconds) {
    List<Block> placed = new ArrayList<>();
    var w = center.getWorld();
    int x = center.getBlockX();
    int y = center.getBlockY();
    int z = center.getBlockZ();
    int groundY = -1;
    for (int dy = 1; dy <= maxDown; dy++) {
        Block b = w.getBlockAt(x, y - dy, z);
        if (b.getType().isSolid()) { groundY = b.getY(); break; }
    }
    int padY = (groundY == -1) ? (y - 1) : (groundY + 1);
    padY = Math.min(Math.max(padY, w.getMinHeight() + 1), w.getMaxHeight() - 2);
    for (int dx = -halfSize; dx <= halfSize; dx++) {
        for (int dz = -halfSize; dz <= halfSize; dz++) {
            Block floor = w.getBlockAt(x + dx, padY, z + dz);
            if (floor.getType().isAir()) { floor.setType(Material.GLASS, false); placed.add(floor); }
        }
    }
    new BukkitRunnable(){ @Override public void run(){
        for (Block b : placed) if (b.getType() == Material.GLASS) b.setType(Material.AIR, false);
    }}.runTaskLater(plugin, 20L * seconds);
    return placed;
}

}
