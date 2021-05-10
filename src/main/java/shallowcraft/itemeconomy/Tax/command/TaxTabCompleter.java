package shallowcraft.itemeconomy.Tax.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class TaxTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Config.TaxSubCommands, completions);
        else if(args.length == 2) {
            if (args[0].equals("add") || args[0].equals("remove") || args[0].equals("info") || args[0].equals("tax") || args[0].equals("clear") || args[0].equals("edit")) {
                StringUtil.copyPartialMatches(args[1], Util.getAllPlayerNames(), completions);
            }
        } else if(args.length == 3){
            if(args[0].equals("add") || args[0].equals("remove") || args[0].equals("info") || args[0].equals("tax") || args[0].equals("edit") ){
                if(Util.isPlayerName(args[1])){

                    String id = Util.getPlayerID(args[1]);
                    if(id != null && ItemEconomy.getInstance().hasAccount(id)){
                        PlayerAccount holder = (PlayerAccount) ItemEconomy.getInstance().getAccounts().get(id);
                        if(holder != null){
                            StringUtil.copyPartialMatches(args[2], new ArrayList<>(holder.getTaxes().keySet()), completions);
                        }
                    }
                }

                if(!args[0].equals("add"))
                    completions.add("all");
            }
        } else if (args.length == 4){
            if(args[0].equals("edit") && !args[2].equals("all")){
                StringUtil.copyPartialMatches(args[3], Config.TaxEditSubCommands, completions);
            } else if(args[0].equals("edit")){
                completions.add("timeset_now");
            }
        }


        return completions;
    }
}
