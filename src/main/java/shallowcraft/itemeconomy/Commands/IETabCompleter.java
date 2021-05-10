package shallowcraft.itemeconomy.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Config.IESubCommands, completions);
        else if(args.length == 2) {
            if (args[0].equals("create_account") || args[0].equals("remove_account") || args[0].equals("deposit") || args[0].equals("withdraw") || args[0].equals("admintransfer")
                    || args[0].equals("balance")) { ;
                StringUtil.copyPartialMatches(args[1], Stream.concat(Util.getAllPlayerNames().stream(),
                        Util.getAllGeneralAccountIDs().stream()).collect(Collectors.toList()), completions);
            } if(args[0].equals("transfer")){
                StringUtil.copyPartialMatches(args[1], Config.transferTypes, completions);
            }
        } else if(args.length == 3){
            if(args[0].equals("transfer") || args[0].equals("admintransfer"))
                StringUtil.copyPartialMatches(args[2], Config.transferTypes, completions);
        } else if(args.length == 4){
            if(args[0].equals("admintransfer"))
                StringUtil.copyPartialMatches(args[3], Config.transferTypes, completions);
        }

        return completions;
    }
}
