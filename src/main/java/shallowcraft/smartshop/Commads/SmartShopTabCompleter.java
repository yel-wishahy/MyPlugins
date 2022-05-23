package shallowcraft.smartshop.Commads;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shallowcraft.itemeconomy.Config;
import shallowcraft.smartshop.SmartShopUtil;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SmartShopTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Config.SS_subCommands, completions);
        else if(args.length == 2) {
            if(args[0].equals("search"))
                StringUtil.copyPartialMatches(args[1], SmartShopUtil.getMaterialsListAsString(), completions);

            if (args[0].equals("info") || args[0].equals("add") || args[0].equals("remove") || args[0].equals("generate") || args[0].equals("log")) { ;
                StringUtil.copyPartialMatches(args[1], Util.getAllPlayerNames(), completions);
            }

            if((args[0].equals("accept") || args[0].equals("decline")) && commandSender instanceof Player){
                StringUtil.copyPartialMatches(args[1], SmartShopUtil.getOrderSummaries(((Player) commandSender).getUniqueId().toString()), completions);
                completions.add("all");
            }

            if(args[0].equals("remove"))
                completions.add("all");
        } else if (args.length == 3 && args[0].equals("remove")){
            completions.add("all");
            StringUtil.copyPartialMatches(args[2], SmartShopUtil.getOrderSummaries(Util.getPlayerID(args[1])), completions);
        }

        return completions;
    }
}
