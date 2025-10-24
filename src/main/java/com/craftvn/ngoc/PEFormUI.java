package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.cumulus.form.SimpleForm;

public class PEFormUI {
    private static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }

    public static void openMainForm(Player p, GemManager m){
        SimpleForm.Builder b = SimpleForm.builder()
                .title(cc("&2&lCửa Hàng Ngọc"))
                .content(cc("&7Chọn nhóm ngọc:"))
                .button(cc("&aBuff"))
                .button(cc("&cCombat"))
                .button(cc("&eĐặc Biệt"))
                .button(cc("&6Totem Dởm"));
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b);
    }

    public static void openGroup(Player p, String title, GemManager m, GemType[] gems){
        SimpleForm.Builder b = SimpleForm.builder().title(cc(title)).content(cc("&7Chọn ngọc muốn mua:"));
        for (GemType g: gems){
            String req = "&eYêu cầu: &f"+g.exp()+" exp"+(g.req()==null?"":" &7+ &f"+g.amount()+" "+g.req().name());
            b.button(cc("&f"+g.display()+"\n"+req));
        }
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b);
    }
}
