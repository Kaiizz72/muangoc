package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class NgocCommand implements CommandExecutor {
    private final GemManager manager;
    private final PCGui pcGui; // <— có field instance

    public NgocCommand(GemManager m){
        this.manager = m;
        this.pcGui = new PCGui(m); // <— TRUYỀN GemManager vào constructor PCGui
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!(sender instanceof Player p)){ sender.sendMessage("Chỉ người chơi mới dùng lệnh này."); return true; }

        if (cmd.getName().equalsIgnoreCase("muangoc")){
            try{
                if (FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId())){
                    // Bedrock (PE): dùng Form
                    PEFormUI.openMainForm(p, manager);
                } else {
                    // PC (Java): dùng Chest GUI
                    pcGui.openMainMenu(p, manager);
                }
            } catch (Throwable t){
                // nếu không có Floodgate -> fallback GUI PC
                pcGui.openMainMenu(p, manager);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("layngoc")){
            if (!p.hasPermission("ngoc.admin")){ p.sendMessage(ChatColor.RED+"Bạn không có quyền."); return true; }
            if (args.length<2){ p.sendMessage(ChatColor.YELLOW+"/layngoc <ten_ngoc> <so_luong>"); return true; }
            try{
                GemType g = GemType.valueOf(args[0].toUpperCase());
                int amount = Integer.parseInt(args[1]);
                for(int i=0;i<amount;i++) manager.giveGem(p,g);
                p.sendMessage(ChatColor.GREEN+"Đã cho bạn "+amount+" "+g.display());
            }catch (Exception e){
                p.sendMessage(ChatColor.RED+"Sai tên ngọc hoặc số lượng!");
            }
            return true;
        }

        return false;
    }
}
