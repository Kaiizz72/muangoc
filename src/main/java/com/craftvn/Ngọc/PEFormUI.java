package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.cumulus.form.SimpleForm;

public class PEFormUI {
    private static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
    private static String pretty(Material m){ return m.name().toLowerCase().replace('_',' '); }

    public static void openMainForm(Player p, GemManager m){
        SimpleForm.Builder b = SimpleForm.builder()
                .title(cc("&2&lCửa Hàng Ngọc"))
                .content(cc("&7Chọn nhóm ngọc:"))
                .button(cc("&aBuff"))
                .button(cc("&cCombat"))
                .button(cc("&eĐặc Biệt"))
                .button(cc("&6Totem"));

        b.validResultHandler(res -> {
            int id = res.clickedButtonId();
            switch (id){
                case 0 -> openGroup(p, m, cc("&aBuff"),
                        new GemType[]{GemType.SINH_LUC, GemType.BAO_VE, GemType.HOI_PHUC, GemType.SINH_MENH, GemType.TANG_MAU});
                case 1 -> openGroup(p, m, cc("&cCombat"),
                        new GemType[]{GemType.CUONG_NO, GemType.HAC_AM, GemType.TRI_TRE, GemType.HO_VE});
                case 2 -> openGroup(p, m, cc("&eĐặc Biệt"),
                        new GemType[]{GemType.LONG_KINH, GemType.SAM_SET, GemType.DICH_CHUYEN, GemType.CAM_BAY, GemType.TRUY_PHONG});
                case 3 -> openGroup(p, m, cc("&6Totem"),
                        new GemType[]{GemType.TOTEM_GIA});
            }
        });

        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b.build());
    }

    private static void openGroup(Player p, GemManager m, String title, GemType[] gems){
        SimpleForm.Builder b = SimpleForm.builder().title(title).content(cc("&7Chọn ngọc muốn mua:"));
        for (GemType g: gems){
            String line = "&f"+g.display()+"\n&8&oYêu cầu: "+g.exp()+" exp"+(g.req()==null?"":" + "+g.amount()+" "+pretty(g.req()))
                    +"\n&8&oCD: "+(g.consume() ? "0s (xài 1 lần)" : g.cooldown()+"s")
                    +(g.expireMinutes()>0?" | hạn: "+g.expireMinutes()+"p":"");
            b.button(cc(line));
        }
        b.validResultHandler(res -> openConfirm(p, m, gems[res.clickedButtonId()]));
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b.build());
    }

    private static void openConfirm(Player p, GemManager m, GemType g){
        String info = "&f"+g.display()+"\n&8&oMô tả: "+g.desc()
                +"\n&8&oYêu cầu: "+g.exp()+" exp"+(g.req()==null?"":" + "+g.amount()+" "+pretty(g.req()))
                +"\n&7Bạn có muốn mua?";
        SimpleForm.Builder b = SimpleForm.builder()
                .title(cc("&eXác nhận mua"))
                .content(cc(info))
                .button(cc("&aMua"))
                .button(cc("&cHủy"));

        b.validResultHandler(res -> {
            if (res.clickedButtonId()==0){
                if (tryBuy(p, g)){
                    p.getInventory().addItem(m.createGemItem(g));
                    p.sendMessage(cc("&aĐã mua: &f"+g.display()));
                }
            } else {
                openMainForm(p, m);
            }
        });

        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b.build());
    }

    private static boolean tryBuy(Player p, GemType g){
        if (p.getLevel() < g.exp()){ p.sendMessage(cc("&cThiếu exp: cần &f"+g.exp())); return false; }
        if (g.req()!=null && !p.getInventory().contains(g.req(), g.amount())){ p.sendMessage(cc("&cThiếu vật phẩm: &f"+g.amount()+" "+pretty(g.req()))); return false; }
        p.setLevel(p.getLevel() - g.exp());
        if (g.req()!=null) p.getInventory().removeItem(new org.bukkit.inventory.ItemStack(g.req(), g.amount()));
        return true;
    }
}
