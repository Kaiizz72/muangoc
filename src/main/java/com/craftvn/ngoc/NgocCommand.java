package com.craftvn.ngoc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.lang.reflect.Method;

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
                // Bedrock (Floodgate) -> mở UI PE nếu có
                try{
                    if (FloodgateApi.getInstance().isFloodgatePlayer(p.getUniqueId())){
                        try { PEFormUI.openMainForm(p, manager); opened = true; } catch (Throwable ignore){}
                    }
                }catch (Throwable ignore){}
                // PC GUI: thử PCGui qua reflection (nhiều biến thể)
                if (!opened) opened = tryOpenPcGuiReflect(p);
                // Fallback đảm bảo
                if (!opened) PCGuiCompat.openMainForm(p, manager);
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

    private boolean tryOpenPcGuiReflect(Player p){
        String[] candidates = new String[]{
            "com.craftvn.ngoc.PCGui",
            "com.craftvn.ngoc.gui.PCGui"
        };
        for (String name : candidates){
            try{
                Class<?> clz = Class.forName(name);
                Object inst = null;
                try { inst = clz.getConstructor(GemManager.class).newInstance(manager); } catch (NoSuchMethodException ignored){}
                try { if (inst==null) inst = clz.getConstructor().newInstance(); } catch (NoSuchMethodException ignored){}
                // Dò method khả dụng
                for (Method m : clz.getDeclaredMethods()){
                    String mn = m.getName().toLowerCase();
                    if (!mn.contains("open")) continue;
                    Class<?>[] pt = m.getParameterTypes();
                    boolean isStatic = java.lang.reflect.Modifier.isStatic(m.getModifiers());
                    try{
                        m.setAccessible(true);
                        if (pt.length==2 && pt[0].isAssignableFrom(Player.class) && pt[1].isAssignableFrom(GemManager.class)){
                            if (isStatic) { m.invoke(null, p, manager); return true; }
                            if (inst!=null) { m.invoke(inst, p, manager); return true; }
                        } else if (pt.length==1 && pt[0].isAssignableFrom(Player.class)){
                            if (isStatic) { m.invoke(null, p); return true; }
                            if (inst!=null) { m.invoke(inst, p); return true; }
                        }
                    }catch (Throwable ignored){}
                }
            }catch (Throwable ignored){}
        }
        return false;
    }
}
