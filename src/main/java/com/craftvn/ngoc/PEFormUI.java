
package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.response.SimpleFormResponse;

public class PEFormUI {
    private static String cc(String s){ return ChatColor.translateAlternateColorCodes('&', s); }

    public static void openMainForm(Player p, GemManager m){
        SimpleForm form = SimpleForm.builder()
                .title(cc("&2&lCửa Hàng Ngọc"))
                .content(cc("&7Chọn nhóm:"))
                .button(cc("&aNgọc Buff"))
                .button(cc("&cNgọc Combat"))
                .button(cc("&eNgọc Đặc Biệt"))
                .button(cc("&6Totem Dởm"))
                .responseHandler((res) -> {
                    if (res == null) return;
                    int id = ((SimpleFormResponse)res).getClickedButtonId();
                    switch (id){
                        case 0 -> openGroup(p, "&aNgọc Buff", m, new GemType[]{GemType.SINH_LUC,GemType.BAO_VE,GemType.HOI_PHUC,GemType.SINH_MENH});
                        case 1 -> openGroup(p, "&cNgọc Combat", m, new GemType[]{GemType.CUONG_NO,GemType.HAC_AM,GemType.TRI_TRE,GemType.HO_VE});
                        case 2 -> openGroup(p, "&eNgọc Đặc Biệt", m, new GemType[]{GemType.TOC_DO,GemType.NHAY_CAO,GemType.LONG_KINH,GemType.SAM_SET,GemType.DICH_CHUYEN,GemType.CAM_BAY,GemType.TRUY_PHONG});
                        case 3 -> openGroup(p, "&6Totem Dởm", m, new GemType[]{GemType.TOTEM_GIA});
                    }
                })
                .build();
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), form);
    }

    public static void openGroup(Player p, String title, GemManager m, GemType[] gems){
        SimpleForm.Builder b = SimpleForm.builder().title(cc(title)).content(cc("&7Chọn ngọc muốn mua:"));
        for (GemType g: gems){
            String req = "&eYêu cầu: &f"+g.exp()+" exp"+(g.req()==null?"":" &7+ &f"+g.amount()+" "+g.req().name());
            b.button(cc("&a"+g.display()+"\n&7➛ &f"+g.desc()+"\n"+req));
        }
        b.responseHandler(res->{
            if (res==null) return;
            int idx = ((SimpleFormResponse)res).getClickedButtonId();
            if (idx<0 || idx>=gems.length) return;
            confirmBuy(p, m, gems[idx]);
        });
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), b.build());
    }

    private static void confirmBuy(Player p, GemManager m, GemType g){
        String req = "&eYêu cầu: &f"+g.exp()+" exp"+(g.req()==null?"":" &7+ &f"+g.amount()+" "+g.req().name());
        SimpleForm form = SimpleForm.builder()
                .title(cc("&eXác nhận mua"))
                .content(cc("&fMua &a"+g.display()+"&f?\n&7➛ &f"+g.desc()+"\n"+req))
                .button(cc("&a✔ Đồng ý"))
                .button(cc("&c✘ Hủy"))
                .responseHandler(res->{
                    if (res==null) return;
                    int id = ((SimpleFormResponse)res).getClickedButtonId();
                    if (id==0){
                        if (p.getLevel()<g.exp()){ p.sendMessage(cc("&cKhông đủ exp!")); return; }
                        if (g.req()!=null && !p.getInventory().contains(g.req(), g.amount())){
                            p.sendMessage(cc("&cThiếu nguyên liệu!")); return;
                        }
                        p.setLevel(p.getLevel()-g.exp());
                        if (g.req()!=null) p.getInventory().removeItem(new org.bukkit.inventory.ItemStack(g.req(), g.amount()));
                        m.giveGem(p, g);
                        p.sendMessage(cc("&aĐã mua &f"+g.display()));
                    }
                }).build();
        FloodgateApi.getInstance().sendForm(p.getUniqueId(), form);
    }
}
