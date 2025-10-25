package com.craftvn.ngoc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PCGui implements Listener {
    private final GemManager manager;

    private static final String TITLE_MAIN = GemManager.cc("&0&lMUA NGỌC &7(PC)");
    private static final String TITLE_BUFF = GemManager.cc("&0&lNGỌC &7- &aHồi phục/Buff");
    private static final String TITLE_COMBAT = GemManager.cc("&0&lNGỌC &7- &cTấn công/Khống chế");
    private static final String TITLE_UTIL = GemManager.cc("&0&lNGỌC &7- &eTiện ích/Đặc biệt");

    public PCGui(GemManager manager){ this.manager = manager; }

    public void openMainMenu(Player p){ openRoot(p); }
    public void openMainForm(Player p){ openRoot(p); }
    public static void openMainMenu(Player p, GemManager m){ new PCGui(m).openRoot(p); }
    public static void openMainForm(Player p, GemManager m){ new PCGui(m).openRoot(p); }

    private void openRoot(Player p){
        Inventory inv = Bukkit.createInventory(p, 27, TITLE_MAIN);
        inv.setItem(11, button(Material.LIME_DYE, "&aHồi phục/Buff", "&7Nhấp để xem các ngọc buff"));
        inv.setItem(13, button(Material.RED_DYE, "&cTấn công/Khống chế", "&7Nhấp để xem các ngọc combat"));
        inv.setItem(15, button(Material.GOLDEN_APPLE, "&eTiện ích/Đặc biệt", "&7Nhấp để xem các ngọc đặc biệt"));
        p.openInventory(inv);
    }

    private void openBuff(Player p){
        List<GemType> list = Arrays.asList(
                GemType.SINH_LUC, GemType.BAO_VE, GemType.HOI_PHUC,
                GemType.TANG_MAU, GemType.SINH_MENH, GemType.TOC_DO, GemType.NHAY_CAO
        );
        openGemList(p, list, TITLE_BUFF);
    }

    private void openCombat(Player p){
        List<GemType> list = Arrays.asList(
                GemType.CUONG_NO, GemType.HAC_AM, GemType.TRI_TRE,
                GemType.HO_VE, GemType.LONG_KINH, GemType.SAM_SET,
                GemType.CAM_BAY, GemType.TRUY_PHONG
        );
        openGemList(p, list, TITLE_COMBAT);
    }

    private void openUtility(Player p){
        List<GemType> list = Arrays.asList(
                GemType.DICH_CHUYEN, GemType.TOTEM_GIA
        );
        openGemList(p, list, TITLE_UTIL);
    }

    private void openGemList(Player p, List<GemType> gems, String title){
        int size = Math.min(54, Math.max(18, ((gems.size() + 8) / 9) * 9));
        Inventory inv = Bukkit.createInventory(p, size, title);
        int i = 0;
        for (GemType g : gems){
            ItemStack it = manager.createGemItem(g);
            ItemMeta meta = it.getItemMeta();
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
            lore.add(GemManager.cc("&8Nhấp để mua"));
            meta.setLore(lore);
            it.setItemMeta(meta);
            inv.setItem(i++, it);
        }
        inv.setItem(size-1, button(Material.ARROW, "&7« Quay lại", "&7Về menu chính"));
        p.openInventory(inv);
    }

    private ItemStack button(Material mat, String name, String lore1){
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(GemManager.cc("&f" + name));
        if (lore1 != null){
            m.setLore(Collections.singletonList(GemManager.cc("&7" + lore1)));
        }
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    private GemType detectGem(ItemStack it){
        if (it == null || !it.hasItemMeta()) return null;
        ItemMeta m = it.getItemMeta();
        if (m.getPersistentDataContainer().has(NgocPlugin.KEY_GEM, org.bukkit.persistence.PersistentDataType.STRING)){
            try{
                String name = m.getPersistentDataContainer().get(NgocPlugin.KEY_GEM, org.bukkit.persistence.PersistentDataType.STRING);
                return GemType.valueOf(name);
            }catch (Exception ignore){}
        }
        String dn = ChatColor.stripColor(m.getDisplayName() == null ? "" : m.getDisplayName()).toLowerCase();
        for (GemType g : GemType.values()){
            String gd = ChatColor.stripColor(GemManager.cc(g.display())).toLowerCase();
            if (dn.contains(ChatColor.stripColor(gd))) return g;
        }
        return null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        if (!(TITLE_MAIN.equals(title) || TITLE_BUFF.equals(title) || TITLE_COMBAT.equals(title) || TITLE_UTIL.equals(title))) return;

        if (e.getRawSlot() < e.getView().getTopInventory().getSize()) e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (TITLE_MAIN.equals(title)){
            Material t = clicked.getType();
            if (t == Material.LIME_DYE) { openBuff(p); return; }
            if (t == Material.RED_DYE) { openCombat(p); return; }
            if (t == Material.GOLDEN_APPLE) { openUtility(p); return; }
            return;
        }

        if (clicked.getType() == Material.ARROW){
            openRoot(p); return;
        }

        GemType g = detectGem(clicked);
        if (g != null){
            manager.tryBuy(p, g);
            if (TITLE_BUFF.equals(title)) openBuff(p);
            else if (TITLE_COMBAT.equals(title)) openCombat(p);
            else if (TITLE_UTIL.equals(title)) openUtility(p);
        }
    }

    @EventHandler public void onClose(InventoryCloseEvent e){}
}
