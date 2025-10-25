package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class NgocCommand implements CommandExecutor {
    private final GemManager manager;

    public NgocCommand(GemManager m){
        this.manager = m;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!(sender instanceof Player p)){
            sender.sendMessage("Chỉ người chơi mới dùng lệnh này.");
            return true;
        }

        String name = cmd.getName().toLowerCase();
        switch (name){
            case "muangoc" -> {
                boolean opened = false;
                // Thử mở UI PE (Floodgate) nếu có
                try{
                    if (FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId())){
                        // Gọi trực tiếp, nếu class/method khác chữ ký thì bắt lỗi và fallback
                        try {
                            PEFormUI.openMainForm(p, manager);
                            opened = true;
                        } catch (Throwable ignored) {}
                    }
                }catch (Throwable ignored){}
                // Fallback: mở GUI PC bằng reflection để tương thích mọi chữ ký
                if (!opened) openPcGuiReflect(p);
                return true;
            }

            case "layngoc" -> {
                if (!p.hasPermission("ngoc.admin")){
                    p.sendMessage(ChatColor.RED + "Bạn không có quyền.");
                    return true;
                }
                if (args.length < 2){
                    p.sendMessage(ChatColor.YELLOW + "/layngoc <ten_ngoc> <so_luong>");
                    return true;
                }
                try{
                    GemType g = GemType.valueOf(args[0].toUpperCase());
                    int amount = Integer.parseInt(args[1]);
                    for (int i = 0; i < amount; i++) manager.giveGem(p, g);
                    p.sendMessage(ChatColor.GREEN + "Đã cho bạn " + amount + " " + ChatColor.stripColor(GemManager.cc(g.display())));
                }catch (Exception e){
                    p.sendMessage(ChatColor.RED + "Sai tên ngọc hoặc số lượng!");
                }
                return true;
            }
        }
        return false;
    }

    /** Dùng reflection để tương thích mọi phiên bản PCGui (tránh lỗi compile do khác chữ ký). */
    private void openPcGuiReflect(Player p){
        try{
            Class<?> guiClz = Class.forName("com.craftvn.ngoc.PCGui");
            Object inst = null;

            // Ưu tiên constructor nhận GemManager
            try{
                inst = guiClz.getConstructor(GemManager.class).newInstance(manager);
            }catch (NoSuchMethodException ignored){}

            // 1) Thử non-static: openMainMenu(Player)
            if (inst != null){
                try{
                    guiClz.getMethod("openMainMenu", Player.class).invoke(inst, p);
                    return;
                }catch (NoSuchMethodException ignored){}
                // 2) Thử non-static: openMainForm(Player)
                try{
                    guiClz.getMethod("openMainForm", Player.class).invoke(inst, p);
                    return;
                }catch (NoSuchMethodException ignored){}
            }

            // Nếu chưa có instance, thử static methods:
            try{
                guiClz.getMethod("openMainForm", Player.class, GemManager.class).invoke(null, p, manager);
                return;
            }catch (NoSuchMethodException ignored){}
            try{
                guiClz.getMethod("openMainMenu", Player.class, GemManager.class).invoke(null, p, manager);
                return;
            }catch (NoSuchMethodException ignored){}

            p.sendMessage(ChatColor.RED + "Không tìm thấy hàm mở GUI trong PCGui.");
        }catch (Throwable t){
            p.sendMessage(ChatColor.RED + "Không mở được GUI PC: " + t.getClass().getSimpleName());
        }
    }
}
