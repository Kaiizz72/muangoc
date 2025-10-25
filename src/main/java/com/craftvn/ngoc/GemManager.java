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
    private final Map<UUID, Long> lastHit = new HashMap<>();
    private final Map<UUID, Map<GemType, Long>> activeUntil = new HashMap<>(); // chỉ dùng cho SINH_LUC chống dồn

    public GemManager(NgocPlugin plugin){ this.plugin = plugin; }

    // ===== Utils =====
    public static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
    private static String small(String raw){ return cc("&f" + ChatColor.stripColor(cc(raw))); } // chữ sáng hơn
    public static String prettyName(Material m){ return m.name().toLowerCase().replace('_',' '); }

    public void markHit(Player p){ lastHit.put(p.getUniqueId(), System.currentTimeMillis()); }
    public boolean recentlyHit(Player p, long ms){
        Long t = lastHit.get(p.getUniqueId());
        return t != null && System.currentTimeMillis() - t < ms;
    }

    private boolean isActive(Player p, GemType g){
        Map<GemType, Long> a = activeUntil.get(p.getUniqueId());
        if (a == null) return false;
        Long t = a.get(g);
        return t != null && System.currentTimeMillis() < t;
    }
    private void setActive(Player p, GemType g, int seconds){
        activeUntil.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
                .put(g, System.currentTimeMillis() + seconds*1000L);
    }

    // ===== Items =====
    public ItemStack createGemItem(GemType gem){
        ItemStack it = new ItemStack(gem==GemType.TOTEM_GIA ? Material.TOTEM_OF_UNDYING : Material.SLIME_BALL);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(cc(gem.display()));

        List<String> lore = new ArrayList<>();
        lore.add(small(gem.desc()));
        String req = "&eYêu cầu: &f" + gem.exp() + " cấp kinh nghiệm";
        if (gem.req()!=null && gem.amount()>0) req += cc(" &7+ &f"+gem.amount()+" "+prettyName(gem.req()));
        lore.add(cc(req));
        if (gem.cooldown()>0){
            String cd = "&7Cooldown: &f" + gem.cooldown() + "s";
            if (gem.expireMinutes()>0) cd += cc(" &7| Hạn: &f"+gem.expireMinutes()+"p");
            lore.add(cc(cd));
        } else if (gem.expireMinutes()>0){
            lore.add(cc("&7Hạn sử dụng: &f"+gem.expireMinutes()+"p"));
        }

        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        m.getPersistentDataContainer().set(NgocPlugin.KEY_GEM, PersistentDataType.STRING, gem.name());
        it.setItemMeta(m);
        return it;
    }

    public void giveGem(Player p, GemType g){ p.getInventory().addItem(createGemItem(g)); }

    // ===== Mua (LEVEL + vật phẩm) =====
    public boolean tryBuy(Player p, GemType g){
        int needLv = g.exp();
        if (p.getLevel() < needLv){
            p.sendMessage(cc("&cThiếu &f" + needLv + " cấp kinh nghiệm&c."));
            return false;
        }
        if (g.req()!=null && g.amount()>0){
            int need = g.amount(), have = 0;
            for (ItemStack slot : p.getInventory().getContents())
                if (slot!=null && slot.getType()==g.req()) have += slot.getAmount();
            if (have < need){
                p.sendMessage(cc("&cThiếu &f"+need+" "+prettyName(g.req())+"&c."));
                return false;
            }
            // consume item
            int left = need;
            for (int i=0;i<p.getInventory().getSize();i++){
                ItemStack slot = p.getInventory().getItem(i);
                if (slot==null || slot.getType()!=g.req()) continue;
                int take = Math.min(left, slot.getAmount());
                slot.setAmount(slot.getAmount()-take);
                if (slot.getAmount()<=0) p.getInventory().setItem(i, null);
                left -= take;
                if (left<=0) break;
            }
        }
        // trừ LEVEL
        p.setLevel(Math.max(0, p.getLevel() - needLv));
        // give
        giveGem(p, g);
        p.sendMessage(cc("&aĐã mua &f"+g.display()+"&a."));
        return true;
    }

    // ===== Kích hoạt ngọc =====
    public void activate(Player p, GemType g){
        // Không có cooldown cho mọi ngọc
        // Riêng SINH_LUC: không cho dùng dồn khi đang có hiệu ứng (2 phút)
        if (g == GemType.SINH_LUC && isActive(p, g)){
            p.sendMessage(cc("&cNgọc &fSinh Lực &cđang hoạt động (2 phút)."));
            return;
        }

        switch (g){
            case SINH_LUC -> {
                // +3 tim tức thì + Regen II trong 120s, xài 1 lần, KHÔNG cho dồn
                AttributeInstance mh = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (mh != null) p.setHealth(Math.min(p.getHealth()+6.0, mh.getBaseValue()));
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*120, 1));
                setActive(p, g, 120);
                fx(p, Sound.ENTITY_PLAYER_LEVELUP, Particle.HEART);
            }
            case BAO_VE -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20*10, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*20, 1));
                fx(p, Sound.BLOCK_BEACON_ACTIVATE, Particle.INSTANT_EFFECT);
            }
            case HOI_PHUC -> {
                AttributeInstance mh = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (mh != null) p.setHealth(mh.getBaseValue());
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*15, 0));
                fx(p, Sound.ENTITY_PLAYER_LEVELUP, Particle.HEART);
            }
            case SINH_MENH -> {
                AttributeInstance a = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (a != null){
                    double old = a.getBaseValue();
                    a.setBaseValue(40.0);
                    p.setHealth(Math.min(40.0, p.getHealth()));
                    new BukkitRunnable(){
                        @Override public void run(){
                            AttributeInstance ax = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (ax != null){
                                ax.setBaseValue(old);
                                if (p.getHealth()>old) p.setHealth(old);
                                p.sendMessage(cc("&cNgọc Sinh Mệnh đã hết hiệu lực."));
                            }
                        }
                    }.runTaskLater(plugin, 20L* (g.expireMinutes()>0? g.expireMinutes()*60L : 300L));
                    fx(p, Sound.ITEM_TOTEM_USE, Particle.HEART);
                }
            }
            case TANG_MAU -> {
                AttributeInstance a = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (a != null){
                    double old = a.getBaseValue();
                    double target = old + 40.0;
                    a.setBaseValue(target);
                    p.setHealth(Math.min(p.getHealth()+40.0, target));
                    new BukkitRunnable(){ @Override public void run(){
                        AttributeInstance ar = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        if (ar != null){
                            ar.setBaseValue(old);
                            if (p.getHealth()>old) p.setHealth(old);
                            p.sendMessage(cc("&eHiệu ứng &cTăng Máu&f đã kết thúc."));
                        }
                    }}.runTaskLater(plugin, 20L*300);
                    fx(p, Sound.ITEM_ARMOR_EQUIP_DIAMOND, Particle.HEART);
                }
            }
            case CUONG_NO -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20*15, 1));
                new BukkitRunnable(){ @Override public void run(){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20*10, 0));
                }}.runTaskLater(plugin, 20L*15);
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
                    new BukkitRunnable(){ @Override public void run(){ v.remove(); }}.runTaskLater(plugin, 20L*20);
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
                makeGlassCageSmart(p.getLocation(), 3, 4, 8);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, 1);
            }
            case SAM_SET -> {
                Player t = nearestPlayer(p, 10);
                if (t != null) p.getWorld().strikeLightning(t.getLocation());
                fx(p, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, Particle.ELECTRIC_SPARK);
            }
            case DICH_CHUYEN -> {
                if (recentlyHit(p, 5000)){ p.sendMessage(cc("&cBạn đang bị đánh, không thể dịch chuyển!")); break; }
                Location dest = findSafeRandomNear(p.getLocation(), 7, 10);
                if (dest == null){
                    p.sendMessage(cc("&cKhông tìm được chỗ an toàn để dịch chuyển. Thử lại!"));
                    break;
                }
                p.teleport(dest);
                fx(p, Sound.ENTITY_ENDERMAN_TELEPORT, Particle.PORTAL);
            }
            case CAM_BAY -> {
                Player target = nearestPlayer(p,7);
                if (target==null){ p.sendMessage(cc("&cKhông có mục tiêu gần!")); break; }
                Block b = target.getLocation().getBlock();
                if (b.getType()==Material.AIR){ b.setType(Material.COBWEB,false); }
                p.teleport(target.getLocation());
                new BukkitRunnable(){ @Override public void run(){
                    if (b.getType()==Material.COBWEB) b.setType(Material.AIR,false);
                }}.runTaskLater(plugin, 20L*5);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_COBWEB_PLACE,1,1);
            }
            case TRUY_PHONG -> {
                Player target = p.getWorld().getNearbyPlayers(p.getLocation(),30).stream()
                        .filter(x->!x.equals(p) && (x.isGliding() || x.getInventory().getChestplate()!=null 
                                && x.getInventory().getChestplate().getType()==Material.ELYTRA))
                        .min(Comparator.comparingDouble(x->x.getLocation().distance(p.getLocation()))).orElse(null);
                if (target==null){ p.sendMessage(cc("&cKhông có mục tiêu dùng/mặc Elytra!")); break; }
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

    // ===== Helpers =====
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

    // Lồng kính mũ úp (không sàn)
    private void makeGlassCageSmart(Location around, int radius, int height, int seconds){
        List<Block> placed = new ArrayList<>();
        World w = around.getWorld();
        int bx = around.getBlockX();
        int bz = around.getBlockZ();

        int baseY;
        Block feet = w.getBlockAt(bx, around.getBlockY(), bz);
        if (feet.getType().isAir()) baseY = w.getHighestBlockYAt(bx, bz);
        else baseY = feet.getY();
        baseY = Math.min(Math.max(baseY, w.getMinHeight() + 1), w.getMaxHeight() - height - 2);
        Location base = new Location(w, bx + 0.5, baseY + 1, bz + 0.5);

        // clear bên trong
        for (int x=-radius+1;x<=radius-1;x++)
            for (int y=0;y<height;y++)
                for (int z=-radius+1;z<=radius-1;z++){
                    Block b = w.getBlockAt(base.clone().add(x,y,z));
                    if (!b.getType().isAir()) b.setType(Material.AIR,false);
                }

        // mái
        for (int x=-radius;x<=radius;x++)
            for (int z=-radius;z<=radius;z++){
                Block roof = w.getBlockAt(base.clone().add(x,height,z));
                if (roof.getType()==Material.AIR){ roof.setType(Material.GLASS,false); placed.add(roof); }
            }
        // tường
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

    private Location findSafeRandomNear(Location origin, int minRadius, int maxRadius){
        World world = origin.getWorld();
        Random r = new Random();
        for (int tries = 0; tries < 16; tries++){
            double angle = r.nextDouble() * Math.PI * 2;
            double dist  = minRadius + r.nextDouble() * (maxRadius - minRadius);
            int x = origin.getBlockX() + (int)Math.round(Math.cos(angle) * dist);
            int z = origin.getBlockZ() + (int)Math.round(Math.sin(angle) * dist);

            Block ground = world.getHighestBlockAt(x, z);
            Material gm = ground.getType();
            if (gm == Material.WATER || gm == Material.LAVA) continue;

            Location candidate = ground.getLocation().add(0.5, 1, 0.5);
            if (candidate.getBlock().getType().isAir() && candidate.clone().add(0,1,0).getBlock().getType().isAir()){
                return candidate;
            }
        }
        return null;
    }
}
