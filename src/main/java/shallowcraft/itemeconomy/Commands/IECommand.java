package shallowcraft.itemeconomy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Data.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;

import java.util.*;

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
                TransactionResult r = new TransactionResult(0, TransactionResult.ResultType.FAILURE, "transfer");
                if (args.length == 4) {
                    if(Util.isPlayerName(sender.getName())){
                        String id = Util.getPlayerID(sender.getName());
                        Account holder = accounts.get(id);
                        VaultType source = Util.getVaultTypeFromArgs(args[1]);
                        VaultType destination = Util.getVaultTypeFromArgs(args[2]);
                        int amount = Integer.parseInt(args[3]);

                        if(holder != null && amount > 0 && source != destination && holder.getBalance(source) >= amount){
                            r = holder.transfer(source, destination, amount);

                            if(holder.getChequingBalance() <= amount && TransactionResult.ResultType.failureModes.contains(r.type))
                                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Encountered insufficient funds in specified vault type");
                        }
                    }
                }

                if(r.type == TransactionResult.ResultType.SUCCESS){
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You successfully transferred " + ChatColor.YELLOW + r.amount +
                            ChatColor.AQUA + " diamonds " + ChatColor.GREEN + " between accounts!");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Transfer failed.");
                }

                return true;
            case "admintransfer":
                TransactionResult result1 = new TransactionResult(0, TransactionResult.ResultType.FAILURE, "transfer");

                if (args.length == 5 && sender.hasPermission(Permissions.adminPerm)) {
                    String name = args[1];
                    VaultType source = Util.getVaultTypeFromArgs(args[2]);
                    VaultType destination = Util.getVaultTypeFromArgs(args[3]);
                    int amount = Integer.parseInt(args[4]);
                    Account holder = null;

                    try {
                        holder = accounts.get(Util.getPlayerID(name));
                    } catch (Exception ignored) {
                    }

                    if(holder != null){
                        ItemEconomy.log.info(amount + "   " + holder.getBalance(source) + " " + source.toString() + " " + destination.toString());
                    }


                    if(holder != null && amount > 0 && source != destination && holder.getBalance(source) >= amount){
                        ItemEconomy.log.info("in here");
                        result1 = holder.transfer(source, destination, amount);

                        if(holder.getChequingBalance() <= amount && TransactionResult.ResultType.failureModes.contains(result1.type))
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Encountered insufficient funds in specified vault type");
                    }
                }

                if(result1.type == TransactionResult.ResultType.SUCCESS){
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully transferred " + ChatColor.YELLOW + result1.amount +
                            ChatColor.AQUA + " diamonds " + ChatColor.GREEN + " between accounts!");
                } else if (!sender.hasPermission(Permissions.adminPerm)) {
                    sender.sendMessage(Permissions.invalidPerm);
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Transfer failed.");
                }

                return true;
            case "create_account":
                boolean addedAccount = false;

                if (args.length == 2) {
                    String name = args[1];
                    OfflinePlayer p = null;

                    if (!Util.isPlayerName(name) && sender.hasPermission(Permissions.adminPerm)) {
                        if (!accounts.containsKey(name)) {
                            accounts.put(name, new GeneralAccount(name));
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW General account ");
                        } else
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "This account exists.");

                        return true;
                    }

                    try {
                        if (!accounts.containsKey(sender.getServer().getPlayerUniqueId(name).toString()))
                            p = sender.getServer().getPlayer(name);
                    } catch (Exception ignored) {
                    }

                    if (p != null) {
                        Account acc = new PlayerAccount(p);
                        accounts.put(acc.getID(), acc);
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account for " +
                                ChatColor.AQUA + name + ChatColor.GREEN + " ! Lucky spending!");
                        addedAccount = true;
                    }
                }

                if (!addedAccount) {
                    if (isPlayer) {
                        if (!accounts.containsKey(player.getUniqueId().toString())) {
                            Account acc = new PlayerAccount(player);
                            accounts.put(acc.getID(), acc);
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account! Lucky spending!");
                            addedAccount = true;
                        }
                    }
                }

                if (!addedAccount)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Failed to create new account.");

                return true;
            case "balance":
                if(args.length == 1 && isPlayer){
                    if (accounts.containsKey(player.getUniqueId().toString())) {
                        Account holder = accounts.get(player.getUniqueId().toString());
                        List<Vault> vaults =  holder.getVaults();
                        int deposit = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.DEPOSIT_ONLY, vaults));
                        int withdraw = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.WITHDRAW_ONLY, vaults));
                        int regular = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.REGULAR, vaults));

                        String rateOfChange = " ";
                        if(holder instanceof PlayerAccount) {
                            rateOfChange = Util.getPercentageBalanceChangeMessage((PlayerAccount) holder);
                        }

                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Your chequing balance is " + ChatColor.YELLOW +
                                holder.getChequingBalance() + " " + ChatColor.AQUA + "Diamonds." + ChatColor.GREEN + " \n Total Holdings: " + ChatColor.YELLOW + holder.getBalance() +
                                rateOfChange + ChatColor.GREEN +
                                "\n Vaults ->  Regular: " + ChatColor.YELLOW + regular +
                                ChatColor.GREEN + " , Deposit: " + ChatColor.YELLOW + deposit + ChatColor.GREEN + " , Withdraw: " + ChatColor.YELLOW + withdraw
                                + ChatColor.GREEN + ".");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You do not have a bank account");
                    }
                } else if (args.length == 2){
                    Account holder = null;

                    try{
                        if(Util.isPlayerName(args[1]))
                            holder = accounts.get(Util.getPlayerID(args[1]));
                        else
                            holder = accounts.get(args[1]);
                    } catch (Exception ignored){
                    }

                    if (holder != null){
                        List<Vault> vaults =  holder.getVaults();
                        int deposit = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.DEPOSIT_ONLY, vaults));
                        int withdraw = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.WITHDRAW_ONLY, vaults));
                        int regular = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.REGULAR, vaults));

                        String rateOfChange = " ";
                        if(holder instanceof PlayerAccount) {
                            rateOfChange = Util.getPercentageBalanceChangeMessage((PlayerAccount) holder);
                        }

                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.YELLOW + holder.getName() + ChatColor.GREEN + "'s chequing balance is " + ChatColor.YELLOW +
                                holder.getChequingBalance() + " " + ChatColor.AQUA + "Diamonds." + ChatColor.GREEN + " \n Total Holdings: " + ChatColor.YELLOW + holder.getBalance() +
                                rateOfChange + ChatColor.GREEN +
                                "\n Vaults ->  Regular: " + ChatColor.YELLOW + regular +
                                ChatColor.GREEN + " , Deposit: " + ChatColor.YELLOW + deposit + ChatColor.GREEN + " , Withdraw: " + ChatColor.YELLOW + withdraw
                                + ChatColor.GREEN + ".");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You do not have a bank account");
                    }
                } else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command here");

                return true;
            case "list_accounts":
                StringBuilder message = new StringBuilder();
                for (Account acc : accounts.values()) {
                    message.append(", ").append(acc.getName());
                }

                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "List of Existing Accounts: " + ChatColor.AQUA + message);

                return true;
            case "create_account_all":
                for (OfflinePlayer p : ItemEconomyPlugin.getInstance().getServer().getOfflinePlayers()) {
                    assert player != null;
                    if (accounts.containsKey(player.getUniqueId().toString())) {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + " is already registered for an account!");
                    } else {
                        Account acc = new PlayerAccount(p);
                        accounts.put(acc.getID(), acc);
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account for " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + "!");
                    }
                }
                return true;
            case "remove_account":
                if (Util.isAdmin(sender)) {
                    if (args.length == 2) {
                        String name = args[1];
                        Account toremove = null;

                        for (Account acc : accounts.values()) {
                            if (acc != null && acc.getName() != null) {
                                if (acc.getName().equals(name))
                                    toremove = acc;
                            }
                        }

                        if (toremove != null) {
                            accounts.remove(toremove.getID());
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + toremove.getName() + "'s" + ChatColor.GREEN + " Account was removed.");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "No such account exists.");
                        }
                    }
                } else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command here");

                return true;
            case "reload":
                if (ItemEconomy.getInstance().loadData())
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully reloaded data");
                else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "FAILED to load data...files corrupt");

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
            case "deposit":
                if (Util.isAdmin(sender)) {
                    if (args.length == 3) {
                        Account holder = null;
                        int amount = 0;

                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception ignore) {
                        }

                        for (Account acc : accounts.values()) {
                            if (acc != null && acc.getName() != null) {
                                if (acc.getName().equals(args[1]) || acc.getID().equals(args[1]))
                                    holder = acc;
                            }
                        }

                        TransactionResult result = null;

                        if (holder != null && amount > 0) {
                            result = holder.deposit(amount);
                        }

                        if (result != null && result.type.equals(TransactionResult.ResultType.SUCCESS)) {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully deposited " + ChatColor.AQUA + result.amount + " diamonds" +
                                    ChatColor.GREEN + " into " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Deposit Failed");
                        }


                    }
                } else
                    sender.sendMessage(Permissions.invalidPerm);

                return true;
            case "withdraw":
                if (Util.isAdmin(sender)) {
                    if (args.length == 3) {
                        Account holder = null;
                        int amount = 0;

                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception ignore) {
                        }

                        for (Account acc : accounts.values()) {
                            if (acc != null && acc.getName() != null) {
                                if (acc.getName().equals(args[1]) || acc.getID().equals(args[1]))
                                    holder = acc;
                            }
                        }

                        TransactionResult result = null;

                        if (holder != null && amount > 0) {
                            result = holder.forcedWithdraw(amount);
                        }

                        if (result != null && result.type.equals(TransactionResult.ResultType.SUCCESS)) {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully withdrew " + ChatColor.AQUA + result.amount + " diamonds" +
                                    ChatColor.GREEN + " from " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Withdraw Failed");
                        }
                    }
                } else
                    sender.sendMessage(Permissions.invalidPerm);

                return true;
            case "statsupdate":
                if(sender.hasPermission(Permissions.adminPerm)){
                    Util.updateServerStats();
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.YELLOW + ChatColor.BOLD + "Updated Server Stats");
                }else
                    sender.sendMessage(Permissions.invalidPerm);

            default:
                return false;
        }
    }
}
