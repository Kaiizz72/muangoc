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
                // Nếu có Floodgate và người chơi là Bedrock -> mở UI PE
                try{
                    if (FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId())){
                        PEFormUI.openMainForm(p, manager);
                        opened = true;
                    }
                }catch (Throwable ignored){}
                // Fallback: GUI PC (Inventory)
                if (!opened) PCGui.openMainForm(p, manager);
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
}
