package shallowcraft.itemeconomy.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shallowcraft.itemeconomy.Data.Config;
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

        return null;
    }

    //    private static boolean onShopCommand(CommandSender sender, Command command, String commandLabel, String[] args, List<PlayerAccount> accounts){
//
//    }

    private static List<String> onIECommandTabComplete(@NotNull String[] args){
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Config.IESubCommands, completions);
        else if(args.length == 2)
            if(args[0].equals("create_account") || args[0].equals("remove_account") || args[0].equals("deposit") || args[0].equals("withdraw"))
                completions = Util.getAllPlayerNames();


        return completions;
    }
}
