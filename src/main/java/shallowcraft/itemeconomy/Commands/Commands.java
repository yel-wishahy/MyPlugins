package shallowcraft.itemeconomy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Accounts.TaxAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

import java.util.*;

public class Commands {
    public static boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args, List<Account> accounts) {
        if(Config.IECommandAliases.contains(commandLabel))
            return onIECommand(sender, command, commandLabel, args, accounts);
        else if (Config.IEShopCommandAliases.contains(commandLabel))
            //fix this
            return false;

        return false;
    }

//    private static boolean onShopCommand(CommandSender sender, Command command, String commandLabel, String[] args, List<PlayerAccount> accounts){
//
//    }

    private static boolean onIECommand(CommandSender sender, Command command, String commandLabel, String[] args, List<Account> accounts){
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

                    if (name.equals("Tax")){
                        if(!Util.hasTaxAccount(accounts)){
                            accounts.add(new TaxAccount());
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW Tax account ");
                        } else
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Tax account exists.");

                        return true;
                    }

                    try{
                        if(!Util.hasAccount(sender.getServer().getPlayer(name).getUniqueId().toString(), accounts))
                            p = sender.getServer().getPlayer(name);
                    } catch (Exception ignored){
                    }

                    if(p != null){
                        accounts.add(new PlayerAccount(p, Config.currency));
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account " +
                                ChatColor.AQUA + name + ChatColor.GREEN + " ! Lucky spending!");
                        addedAccount = true;
                    }
                }

                if(!addedAccount){
                    if (isPlayer) {
                        if (!Util.hasAccount(player.getUniqueId().toString(), accounts)) {
                            accounts.add(new PlayerAccount(player, Config.currency));
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
                    if (Util.hasAccount(player.getUniqueId().toString(), accounts)) {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Your balance is: " + ChatColor.AQUA + Objects.requireNonNull(Util.getAccount(player.getUniqueId().toString(), accounts)).getBalance() + " " + Config.currency.name().toLowerCase());
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You do not have a bank account");
                    }
                } else
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command here");

                return true;
            case "list_accounts":
                StringBuilder message = new StringBuilder();
                for (Account acc : accounts) {
                    message.append(", ").append(acc.getName());
                }

                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "List of Existing Accounts: " + ChatColor.AQUA + message);

                return true;
            case "create_account_all":
                for (OfflinePlayer p : ItemEconomy.getInstance().getServer().getOfflinePlayers()) {
                    if (Util.hasAccount(player.getUniqueId().toString(), accounts)) {
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + " is already registered for an account!");
                    } else {
                        accounts.add(new PlayerAccount(p, Config.currency));
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account for " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + "!");
                    }
                }
                return true;
            case "remove_account":
                if(isAdmin(sender)){
                    if (args.length == 2) {
                        String name = args[1];
                        Account toremove = null;

                        for (Account acc : accounts) {
                            if(acc != null && acc.getName() != null){
                                if (acc.getName().equals(name))
                                    toremove = acc;
                            }
                        }

                        if (toremove != null) {
                            accounts.remove(toremove);
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

                for (Account acc : accounts) {
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

                        for (Account acc : accounts) {
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

                        for (Account acc : accounts) {
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
        } else return ((Player) sender).hasPermission("itemeconomy.admin");
    }

    private static boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    public @Nullable static List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(Config.IECommandAliases.contains(alias))
            return onIECommandTabComplete(args);

        return null;
    }

    private static List<String> onIECommandTabComplete(@NotNull String[] args){
        List<String> completions = new ArrayList<>();

        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Config.IECommands, completions);
        else if(args.length == 2)
            if(args[0].equals("create_account") || args[0].equals("remove_account") || args[0].equals("deposit") || args[0].equals("withdraw"))
                completions = Util.getAllPlayerNames();


        return completions;
    }
}
