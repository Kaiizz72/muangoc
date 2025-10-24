package com.craftvn.ngoc;

import org.bukkit.Material;

public enum GemType {
    // display, desc, exp, req, amount, cooldownSec, consumeOnUse, expireMinutes
    SINH_LUC("&aNgọc Sinh Lực", "Hồi 3 tim & Regen II 10s", 10, Material.COAL, 10, 20, true, 0),
    BAO_VE("&aNgọc Bảo Vệ", "Resistance II 10s + Absorption", 20, Material.IRON_BLOCK, 32, 30, true, 0),
    HOI_PHUC("&aNgọc Hồi Phục", "Full máu + Regen I 15s", 30, Material.DIAMOND, 5, 60, true, 0),
    SINH_MENH("&aNgọc Sinh Mệnh", "+2 thanh máu (tối đa 10p)", 50, Material.NETHERITE_BLOCK, 1, 120, false, 10),

    // NGỌC MỚI
    TANG_MAU("&cNgọc Tăng Máu", "Tăng 2 hàng máu trong 2 phút (CD 30s). Ngọc tự hủy sau 20 phút.", 20, Material.BEACON, 5, 30, false, 20),

    CUONG_NO("&cNgọc Cuồng Nộ", "Strength II 15s rồi Weakness 10s", 20, Material.IRON_BLOCK, 16, 40, true, 0),
    HAC_AM("&cNgọc Hắc Ám", "Wither II 5s + Blindness 3s (5 block)", 30, Material.DIAMOND, 3, 40, true, 0),
    TRI_TRE("&cNgọc Trì Trệ", "Slowness II + Fatigue 5s (7 block)", 30, Material.DIAMOND_BLOCK, 1, 40, true, 0),
    HO_VE("&cNgọc Hộ Vệ (Vex)", "Triệu hồi 3 Vex dí mục tiêu 20s", 50, Material.BEACON, 1, 60, true, 0),

    TOC_DO("&eNgọc Tốc Độ", "Speed II 20s + Jump I 10s", 10, Material.COAL, 5, 20, true, 0),
    NHAY_CAO("&eNgọc Nhảy Cao", "Jump II 20s + Speed I 20s", 10, Material.COAL, 5, 20, true, 0),
    LONG_KINH("&eNgọc Lồng Kính", "Lồng kính 7 block trong 8s", 20, Material.IRON_BLOCK, 16, 60, true, 0),
    SAM_SET("&eNgọc Sấm Sét", "Gọi sét mục tiêu gần", 30, Material.DIAMOND, 2, 60, true, 0),
    DICH_CHUYEN("&eNgọc Dịch Chuyển", "TP ngẫu nhiên 7–10 block", 30, Material.DIAMOND_BLOCK, 1, 60, true, 0),
    CAM_BAY("&eNgọc Cạm Bẫy", "Spawn tơ nhện + TP tới nạn nhân", 40, Material.NETHERITE_BLOCK, 1, 60, true, 0),
    TRUY_PHONG("&eNgọc Truy Phong", "Kéo người dùng Elytra về phía bạn", 50, Material.BEACON, 1, 60, true, 0),

    TOTEM_GIA("&6Totem Dởm", "Như totem thật, chỉ 1 tim vàng", 5, Material.TOTEM_OF_UNDYING, 1, 0, false, 0);

    private final String display, desc;
    private final int expCost, cooldownSec, expireMinutes, amount;
    private final boolean consumeOnUse;
    private final Material requirement;

    GemType(String display, String desc, int expCost, Material requirement, int amount,
            int cooldownSec, boolean consumeOnUse, int expireMinutes){
        this.display = display; this.desc = desc;
        this.expCost = expCost; this.requirement = requirement; this.amount = amount;
        this.cooldownSec = cooldownSec; this.consumeOnUse = consumeOnUse; this.expireMinutes = expireMinutes;
    }

    public String display(){ return display; }
    public String desc(){ return desc; }
    public int exp(){ return expCost; }
    public Material req(){ return requirement; }
    public int amount(){ return amount; }
    public int cooldown(){ return cooldownSec; }
    public boolean consume(){ return consumeOnUse; }
    public int expireMinutes(){ return expireMinutes; }
}
