package shallowcraft.itemeconomy.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class IETabCompleter implements org.bukkit.command.TabCompleter {
    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(Config.IECommandAliases.contains(alias))
            return onIECommandTabComplete(args);
        if(Config.TaxCommandAliases.contains(alias))
            return onTaxCommandComplete(args);

        return null;
    }

    //    private static boolean onShopCommand(CommandSender sender, Command command, String commandLabel, String[] args, List<PlayerAccount> accounts){
//
//    }

    private static List<String> onIECommandTabComplete(@NotNull String[] args){
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Config.IESubCommands, completions);
        else if(args.length == 2) {
            if (args[0].equals("create_account") || args[0].equals("remove_account") || args[0].equals("deposit") || args[0].equals("withdraw")) {
                completions = Util.getAllPlayerNames();
            }
        }

        return completions;
    }

    private  static List<String> onTaxCommandComplete(@NotNull String[] args){
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Config.TaxSubCommands, completions);
        else if(args.length == 2) {
            if (args[0].equals("add_tax") || args[0].equals("remove_tax") || args[0].equals("tax_info") || args[0].equals("tax") || args[0].equals("clear_tax")) {
                completions = Util.getAllPlayerNames();
            }
        } else if(args.length == 3){
            if(args[0].equals("add_tax") || args[0].equals("remove_tax") || args[0].equals("tax_info") || args[0].equals("tax")){
                if(Util.isPlayerName(args[1])){

                    String id = Util.getPlayerID(args[1]);
                    if(id != null && ItemEconomy.getInstance().hasAccount(id)){
                        PlayerAccount holder = (PlayerAccount) ItemEconomy.getInstance().getAccounts().get(id);
                        if(holder != null){
                            completions = new ArrayList<>(holder.getTaxes().keySet());
                        }
                    }
                }
                completions.add("all");
            }
        }


        return completions;

    }
}
