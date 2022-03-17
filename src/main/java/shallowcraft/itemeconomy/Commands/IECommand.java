package shallowcraft.itemeconomy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;

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
                if (ItemEconomy.getInstance().loadData())
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully loaded data");
                else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "FAILED to load data...files corrupt");

                return true;
            case "reload":
                if (ItemEconomy.getInstance().saveData() && ItemEconomy.getInstance().loadData())
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully reloaded data");
                else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "FAILED to reload data...files corrupt");

                return true;
            case "save":
                if (ItemEconomy.getInstance().saveData())
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully saved data");
                else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "FAILED to save data...files corrupt");
                return true;
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
            default:
                return false;
        }
    }
}
