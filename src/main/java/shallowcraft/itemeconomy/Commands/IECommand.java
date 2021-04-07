package shallowcraft.itemeconomy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Accounts.TaxAccount;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

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
        Map<String,Account> accounts = ItemEconomy.getInstance().getAccounts();
        if(Config.IECommandAliases.contains(label))
            return onIECommand(sender, command, label, args, accounts);
        else if (Config.IEShopCommandAliases.contains(label))
            //fix this
            return false;

        return false;
    }

    private static boolean onIECommand(CommandSender sender, Command command, String commandLabel, String[] args, Map<String,Account>  accounts){
        boolean isPlayer = isPlayer(sender);
        Player player = null;

        if (isPlayer)
            player = (Player) sender;

        if (args.length < 1)
            return false;

        switch (args[0]) {
            case "create_account":
                boolean addedAccount = false;

                if(args.length == 2){
                    String name = args[1];
                    OfflinePlayer p = null;

                    if (name.equals(Config.taxID)){
                        if(!accounts.containsKey(Config.taxID)){
                            accounts.put(Config.taxID,new TaxAccount());
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW Tax account ");
                        } else
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Tax account exists.");

                        return true;
                    }

                    try{
                        if(!accounts.containsKey(sender.getServer().getPlayerUniqueId(name).toString()))
                            p = sender.getServer().getPlayer(name);
                    } catch (Exception ignored){
                    }

                    if(p != null){
                        Account acc = new PlayerAccount(p, Config.currency);
                        accounts.put(acc.getID(), acc);
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account " +
                                ChatColor.AQUA + name + ChatColor.GREEN + " ! Lucky spending!");
                        addedAccount = true;
                    }
                }

                if(!addedAccount){
                    if (isPlayer) {
                        if (!accounts.containsKey(player.getUniqueId().toString())) {
                            Account acc = new PlayerAccount(player, Config.currency);
                            accounts.put(acc.getID(), acc);
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account! Lucky spending!");
                            addedAccount = true;
                        }
                    }
                }

                if(!addedAccount)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Failed to create new account.");

                return true;
            case "balance":
                if(isPlayer){
                    if (accounts.containsKey(player.getUniqueId().toString())) {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Your balance is: " + ChatColor.AQUA + accounts.get(player.getUniqueId().toString()).getBalance() + " " + Config.currency.name().toLowerCase());
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
                for (OfflinePlayer p : ItemEconomy.getInstance().getServer().getOfflinePlayers()) {
                    assert player != null;
                    if (accounts.containsKey(player.getUniqueId().toString())) {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + " is already registered for an account!");
                    } else {
                        Account acc = new PlayerAccount(p, Config.currency);
                        accounts.put(acc.getID(), acc);
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account for " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + "!");
                    }
                }
                return true;
            case "remove_account":
                if(isAdmin(sender)){
                    if (args.length == 2) {
                        String name = args[1];
                        Account toremove = null;

                        for (Account acc : accounts.values()) {
                            if(acc != null && acc.getName() != null){
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
                StringBuilder baltopMessage = new StringBuilder();
                baltopMessage.append(ChatColor.GOLD).append("[ItemEconomy] ").append(ChatColor.GREEN).append("Global Player Balances: \n");
                Map<String, Integer> bals = new HashMap<>();

                for (Account acc : accounts.values()) {
                    if (acc != null)
                        bals.put(acc.getName(), acc.getBalance());
                }

                bals = Util.sortByValue(bals);

                List<String> names = new ArrayList<>(bals.keySet());
                int j = 1;
                for (int i = names.size() - 1; i >= 0; i--) {
                    String name = names.get(i);
                    if (name != null) {
                        baltopMessage.append(j).append(". ").append(ChatColor.GOLD).append(name).append(" ".repeat(20 - name.length()));
                        baltopMessage.append(ChatColor.AQUA).append(bals.get(name)).append(" ").append(Config.currency.name().toLowerCase()).append("\n");
                        j++;
                    }
                }

                sender.sendMessage(baltopMessage.toString());
                return true;
            case "deposit":
                if(isAdmin(sender)){
                    if(args.length == 3){
                        Account holder = null;
                        int amount = 0;

                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception ignore){
                        }

                        for (Account acc : accounts.values()) {
                            if(acc != null && acc.getName() != null){
                                if (acc.getName().equals(args[1]))
                                    holder = acc;
                            }
                        }

                        TransactionResult result = null;

                        if(holder != null && amount > 0){
                           result = holder.deposit(amount);
                        }

                        if(result != null && result.type.equals(ResultType.SUCCESS)){
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully deposited " + ChatColor.AQUA + result.amount + " diamonds" +
                                    ChatColor.GREEN + " into " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Deposit Failed");
                        }


                    }
                } else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command here");

                return true;
            case "withdraw":
                if(isAdmin(sender)){
                    if(args.length == 3){
                        Account holder = null;
                        int amount = 0;

                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception ignore){
                        }

                        for (Account acc : accounts.values()) {
                            if(acc != null && acc.getName() != null){
                                if (acc.getName().equals(args[1]))
                                    holder = acc;
                            }
                        }

                        TransactionResult result = null;

                        if(holder != null && amount > 0){
                           result = holder.withdraw(amount);
                        }

                        if(result != null && result.type.equals(ResultType.SUCCESS)){
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully withdrew " + ChatColor.AQUA + result.amount + " diamonds" +
                                    ChatColor.GREEN + " from " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Withdraw Failed");
                        }
                    }
                } else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command here");

                return true;
            default:
                return false;
        }
    }


    private static boolean isAdmin(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        } else return sender.hasPermission("itemeconomy.admin");
    }

    private static boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }
}
