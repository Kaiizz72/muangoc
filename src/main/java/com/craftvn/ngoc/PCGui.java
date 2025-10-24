
package com.craftvn.ngoc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PCGui implements Listener {
    private final GemManager manager;
    public PCGui(GemManager m){ this.manager = m; }

    public static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }

    public static ItemStack item(Material m, String name, String... lore){
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(cc(name));
        List<String> l = new ArrayList<>();
        for (String s: lore) l.add(cc(s));
        meta.setLore(l);
        it.setItemMeta(meta);
        return it;
    }

    public void openMainMenu(Player p, GemManager m){
        Inventory inv = Bukkit.createInventory(null, 27, cc("&2&lCửa Hàng Ngọc"));
        ItemStack glass = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for(int i=0;i<27;i++) inv.setItem(i, glass);
        inv.setItem(11, item(Material.SLIME_BALL, "&aNgọc Buff", "&7➛ Các ngọc buff bản thân"));
        inv.setItem(13, item(Material.SLIME_BALL, "&cNgọc Combat", "&7➛ Ngọc PvP tấn công"));
        inv.setItem(15, item(Material.SLIME_BALL, "&eNgọc Đặc Biệt", "&7➛ Kỹ năng đặc biệt"));
        inv.setItem(22, item(Material.TOTEM_OF_UNDYING, "&6Totem Dởm", "&7➛ Totem giả 1 tim vàng"));
        p.openInventory(inv);
    }

    public static void openGroup(Player p, String title, GemManager m, GemType[] gems){
        Inventory inv = Bukkit.createInventory(null, 54, cc(title));
        int slot=10;
        for (GemType g: gems){
            inv.setItem(slot, m.createGemItem(g));
            slot++;
            if (slot % 9 == 7) slot += 4;
        }
        p.openInventory(inv);
    }

    @EventHandler public void click(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = ChatColor.stripColor(e.getView().getTitle());
        if (title==null) return;
        if (title.equals("Cửa Hàng Ngọc")){
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it==null || !it.hasItemMeta()) return;
            String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
            if (name==null) return;
            if (name.equals("Ngọc Buff")){
                openGroup(p, "&aNgọc Buff", NgocPlugin.get().manager(), new GemType[]{GemType.SINH_LUC,GemType.BAO_VE,GemType.HOI_PHUC,GemType.SINH_MENH, GemType.TANG_MAU});
            } else if (name.equals("Ngọc Combat")){
                openGroup(p, "&cNgọc Combat", NgocPlugin.get().manager(), new GemType[]{GemType.CUONG_NO,GemType.HAC_AM,GemType.TRI_TRE,GemType.HO_VE});
            } else if (name.equals("Ngọc Đặc Biệt")){
                openGroup(p, "&eNgọc Đặc Biệt", NgocPlugin.get().manager(), new GemType[]{GemType.TOC_DO,GemType.NHAY_CAO,GemType.LONG_KINH,GemType.SAM_SET,GemType.DICH_CHUYEN,GemType.CAM_BAY,GemType.TRUY_PHONG});
            } else if (name.equals("Totem Dởm")){
                openGroup(p, "&6Totem Dởm", NgocPlugin.get().manager(), new GemType[]{GemType.TOTEM_GIA});
            }
            return;
        }
        if (title.startsWith("Ngọc ") || title.contains("Totem")){
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it==null || !it.hasItemMeta()) return;
            String disp = ChatColor.stripColor(it.getItemMeta().getDisplayName());
            for (GemType g: GemType.values()){
                if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', g.display())).equals(disp)){
                    // buy flow
                    if (p.getLevel() < g.exp()){ p.sendMessage(cc("&cKhông đủ exp (&f"+g.exp()+"&c)!")); return; }
                    if (g.req()!=null && !p.getInventory().contains(g.req(), g.amount())){
                        p.sendMessage(cc("&cThiếu nguyên liệu: &f"+g.amount()+" "+GemManager.prettyName(g.req()))); return;
                    }
                    p.setLevel(p.getLevel() - g.exp());
                    if (g.req()!=null) p.getInventory().removeItem(new ItemStack(g.req(), g.amount()));
                    NgocPlugin.get().manager().giveGem(p, g);
                    p.sendMessage(cc("&aĐã mua &f"+g.display()));
                    return;
                }
            }
        }
    }
}
