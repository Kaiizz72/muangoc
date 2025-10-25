
package com.craftvn.ngoc;

import org.bukkit.command.*;
import java.util.*;

public class NgocTabCompleter implements TabCompleter {
    @Override public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
        if (cmd.getName().equalsIgnoreCase("layngoc") && args.length==1){
            List<String> list = new ArrayList<>();
            for (GemType g: GemType.values()){
                String s = g.name().toLowerCase();
                if (s.startsWith(args[0].toLowerCase())) list.add(s);
            }
            return list;
        }
        return Collections.emptyList();
    }
}
