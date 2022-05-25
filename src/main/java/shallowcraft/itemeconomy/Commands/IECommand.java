package shallowcraft.itemeconomy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Util.Util;

import java.util.*;

/**
 * implementation of command executor for item economy
 * handles all item economy commands
 * Author: Yousif El-Wishahy
 *
 */
public class IECommand implements CommandExecutor {

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean isPlayer = Util.isPlayer(sender);
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();
        Player player = null;

        if (isPlayer)
            player = (Player) sender;

        if (args.length < 1)
            return false;

        switch (args[0]) {
            case "transfer":
                return Commands.transfer(args,sender,accounts);

            case "admintransfer":
                return Commands.adminTransfer(args,sender,accounts);

            case "create_account":
                return Commands.createAccount(args,sender,accounts,isPlayer,player);

            case "balance":
                return Commands.balance(args,sender,accounts,isPlayer,player);

            case "list_accounts":
                return Commands.listAccounts(sender,accounts);

            case "create_account_all":
                return Commands.createAccountAll(sender,accounts,player);

            case "remove_account":
                return Commands.removeAccount(args,sender,accounts);
            case "load":
                return Commands.load(sender);
            case "reload":
                if (Util.isAdmin(sender)){
                    Commands.save(sender);
                    Commands.load(sender);
                } else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You have no rights to send this command.");
                return true;
            case "save":
                return Commands.save(sender);
            case "baltop":
                String baltopMessage = Util.getServerStatsMessage();
                sender.sendMessage(baltopMessage);
                return true;
            case "admindeposit":
                return Commands.adminDeposit(args,sender,accounts);

            case "adminwithdraw":
                return Commands.adminWithdraw(args,sender,accounts);

            case "statsupdate":
                if(sender.hasPermission(Permissions.adminPerm)){
                    Util.updateServerStats();
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.YELLOW + ChatColor.BOLD + "Updated Server Stats");
                }else
                    sender.sendMessage(Permissions.invalidPerm);
            case "withdraw":
                return Commands.withdraw(args,sender);
            case "debug":
                if(sender.hasPermission(Permissions.adminPerm) || sender.isOp() || !isPlayer){
                    ItemEconomy.getInstance().setDebugMode(!ItemEconomy.getInstance().isDebugMode());
                    if(ItemEconomy.getInstance().isDebugMode())
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + ChatColor.BOLD + "Debug Mode Enabled.");
                    else
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + ChatColor.BOLD + "Debug Mode Disabled.");
                }
                return true;
            case "createconfig":
                if(sender.hasPermission(Permissions.adminPerm) || sender.isOp() || !isPlayer) {
                    try {
                        Config.createConfig();
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + ChatColor.BOLD + "Created " + Config.configFileName + ".yml File");
                    } catch (Exception e) {
                        if (ItemEconomy.getInstance().isDebugMode())
                            e.printStackTrace();
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + ChatColor.BOLD + "Failed to create config file. Enable debug mode for stack trace.");
                    }
                }
                return true;
            default:
                return false;
        }
    }
}
