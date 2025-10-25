package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Arrays;
import java.util.List;

public class PEFormUI {
    private static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
    private static String pretty(Material m){ return m.name().toLowerCase().replace('_',' '); }

    public static void openMainForm(Player p, GemManager m){
        SimpleForm.Builder b = SimpleForm.builder()
                .title(cc("&2&lCửa Hàng Ngọc &7(PE)"))
                .content(cc("&aChọn nhóm ngọc:&r\n&f- &aHồi phục/Buff\n&f- &cTấn công/Khống chế\n&f- &eTiện ích/Đặc biệt"))
                .button(cc("&aHồi phục/Buff"))
                .button(cc("&cTấn công/Khống chế"))
                .button(cc("&eTiện ích/Đặc biệt"))
                .validResultHandler((form, res) -> {
                    int i = res.getClickedButtonId();
                    switch (i){
                        case 0 -> openGroup(p, m, cc("&aHồi phục/Buff"), Arrays.asList(
                                GemType.SINH_LUC, GemType.BAO_VE, GemType.HOI_PHUC, GemType.TANG_MAU, GemType.SINH_MENH, GemType.TOC_DO, GemType.NHAY_CAO
                        ));
                        case 1 -> openGroup(p, m, cc("&cTấn công/Khống chế"), Arrays.asList(
                                GemType.CUONG_NO, GemType.HAC_AM, GemType.TRI_TRE, GemType.HO_VE, GemType.LONG_KINH, GemType.SAM_SET, GemType.CAM_BAY, GemType.TRUY_PHONG
                        ));
                        case 2 -> openGroup(p, m, cc("&eTiện ích/Đặc biệt"), Arrays.asList(
                                GemType.DICH_CHUYEN, GemType.TOTEM_GIA
                        ));
                    }
                });

        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b.build());
    }

    private static void openGroup(Player p, GemManager m, String title, List<GemType> gems){
        SimpleForm.Builder b = SimpleForm.builder().title(title).content(cc("&bChọn ngọc muốn mua:"));
        for (GemType g: gems){
            b.button(cc("&f" + g.display()));
        }
        b.button(cc("&7« Quay lại"));
        b.validResultHandler((form, res) -> {
            int id = res.getClickedButtonId();
            if (id == gems.size()){
                openMainForm(p, m);
                return;
            }
            GemType chosen = gems.get(id);
            openConfirm(p, m, title, gems, chosen);
        });
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b.build());
    }

    private static void openConfirm(Player p, GemManager m, String backTitle, List<GemType> gems, GemType g){
        StringBuilder sb = new StringBuilder();
        sb.append("&f").append(g.display())
          .append("\n&7Mô tả: &f").append(g.desc())
          .append("\n&6Yêu cầu:&r &f").append(g.exp()).append(" cấp kinh nghiệm");
        if (g.req()!=null && g.amount()>0){
            sb.append(" &f+ ").append(g.amount()).append(" ").append(pretty(g.req()));
        }
        if (g.expireMinutes()>0){
            sb.append("\n&dHạn dùng:&r &f").append(g.expireMinutes()).append(" phút");
        }
        SimpleForm.Builder b = SimpleForm.builder()
                .title(cc("&eXác nhận mua"))
                .content(cc(sb.toString()))
                .button(cc("&aXác nhận mua"))
                .button(cc("&7« Quay lại"));
        b.validResultHandler((form, res) -> {
            int id = res.getClickedButtonId();
            if (id == 0){
                m.tryBuy(p, g);
                openGroup(p, m, backTitle, gems);
            } else {
                openGroup(p, m, backTitle, gems);
            }
        });
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b.build());
    }
}
