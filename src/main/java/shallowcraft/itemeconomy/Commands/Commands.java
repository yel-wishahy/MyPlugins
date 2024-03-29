package shallowcraft.itemeconomy.Commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.BankVault.VaultType;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Permissions;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Util.Util;

import java.util.Map;

//implementations for the item economy commands
public class Commands {
    public static boolean transfer(String[] args, CommandSender sender, Map<String, Account> accounts) {
        Transaction r = new Transaction(0, Transaction.ResultType.FAILURE, "transfer");
        if (args.length == 4) {
            if (Util.isPlayerName(sender.getName())) {
                String id = Util.getPlayerID(sender.getName());
                Account holder = accounts.get(id);
                VaultType source = Util.getVaultTypeFromArgs(args[1]);
                VaultType destination = Util.getVaultTypeFromArgs(args[2]);
                int amount = Integer.parseInt(args[3]);

                if (holder != null && amount > 0 && source != destination && holder.getBalance(source) >= amount) {
                    r = holder.transfer(source, destination, amount);

                    if (holder.getBalance(VaultType.REGULAR) <= amount && Transaction.ResultType.failureModes.contains(r.resultType))
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Encountered insufficient funds in specified vault type");
                }
            }
        }

        if (r.resultType == Transaction.ResultType.SUCCESS) {
            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You successfully transferred " + ChatColor.YELLOW + r.amount +
                    ChatColor.AQUA + " diamonds " + ChatColor.GREEN + " between accounts!");
        } else {
            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Transfer failed.");
        }

        return true;
    }

    public static boolean adminTransfer(String[] args, CommandSender sender, Map<String, Account> accounts) {
        Transaction result1 = new Transaction(0, Transaction.ResultType.FAILURE, "transfer");

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

            if (holder != null) {
                ItemEconomy.log.info(amount + "   " + holder.getBalance(source) + " " + source.toString() + " " + destination.toString());
            }


            if (holder != null && amount > 0 && source != destination && holder.getBalance(source) >= amount) {
                //ItemEconomy.log.info("in here");
                result1 = holder.transfer(source, destination, amount);

                if (holder.getBalance(VaultType.REGULAR) <= amount && Transaction.ResultType.failureModes.contains(result1.resultType))
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Encountered insufficient funds in specified vault type");
            }
        }

        if (result1.resultType == Transaction.ResultType.SUCCESS) {
            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully transferred " + ChatColor.YELLOW + result1.amount +
                    ChatColor.AQUA + " diamonds " + ChatColor.GREEN + " between accounts!");
        } else if (!sender.hasPermission(Permissions.adminPerm)) {
            sender.sendMessage(Permissions.invalidPerm);
        } else {
            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Transfer failed.");
        }

        return true;
    }

    public static boolean createAccount(String[] args, CommandSender sender, Map<String, Account> accounts, boolean isPlayer, Player player) {
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
    }

    public static boolean balance(String[] args, CommandSender sender, Map<String, Account> accounts, boolean isPlayer, Player player) {
        if (args.length == 1 && isPlayer) {
            if (accounts.containsKey(player.getUniqueId().toString())) {
                Account holder = accounts.get(player.getUniqueId().toString());
                sender.sendMessage(Util.getBalanceMessage(holder));
            } else {
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You do not have a bank account");
            }
        } else if (args.length == 2) {
            Account holder = null;

            try {
                if (Util.isPlayerName(args[1]))
                    holder = accounts.get(Util.getPlayerID(args[1]));
                else
                    holder = accounts.get(args[1]);
            } catch (Exception ignored) {
            }

            if (holder != null) {
                sender.sendMessage(Util.getBalanceMessage(holder));
            } else {
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You do not have a bank account");
            }
        } else
            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command here");

        return true;

    }

    public static boolean withdraw(String[] args, CommandSender sender) {
        if(Util.isAdmin(sender) ||  sender.hasPermission(Permissions.remoteWithdraw)) {
            if (args.length == 2) {
                PlayerAccount holder = null;
                try {
                    holder = (PlayerAccount) ItemEconomy.getInstance().getAccount(sender.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int amount = 0;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (Exception ignore) {
                }

                Transaction resultWithdraw = null;
                Transaction resultDeposit = null;

                if (holder != null && amount > 0) {
                    resultWithdraw = holder.withdraw(amount,VaultType.ALL);
                    resultDeposit = holder.depositInventory(resultWithdraw.amount);
                    if (resultDeposit.amount < resultWithdraw.amount) {
                        holder.deposit(resultWithdraw.amount - resultDeposit.amount);
                    }
                }

                if (resultWithdraw != null && resultWithdraw.resultType.equals(Transaction.ResultType.SUCCESS)) {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully withdrew " + ChatColor.AQUA + resultDeposit.amount + " diamonds" +
                            ChatColor.GREEN + " from your vault");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Withdraw Failed");
                }
            }
        }

        return true;
    }

    public static boolean deposit(String[] args, CommandSender sender) {
        if(Util.isAdmin(sender) ||  sender.hasPermission(Permissions.remoteDeposit)) {
            if (args.length == 2) {
                PlayerAccount holder = null;
                try {
                    holder = (PlayerAccount) ItemEconomy.getInstance().getAccount(sender.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int amount = 0;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (Exception ignore) {
                }

                Transaction result = null;

                if (holder != null && amount > 0) {
                    result = holder.deposit(amount);
                }

                if (result != null && result.resultType.equals(Transaction.ResultType.SUCCESS)) {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully deposited " + ChatColor.AQUA + result.amount + " diamonds" +
                            ChatColor.GREEN + " into your vault");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Deposit Failed");
                }
            }
        }

        return true;
    }

    public static boolean listAccounts(CommandSender sender, Map<String, Account> accounts){
        StringBuilder message = new StringBuilder();
        for (Account acc : accounts.values()) {
            message.append(", ").append(acc.getName());
        }

        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "List of Existing Accounts: " + ChatColor.AQUA + message);

        return true;
    }

    public static boolean createAccountAll(CommandSender sender, Map<String, Account> accounts, Player player){
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
    }

    public static boolean removeAccount(String[] args, CommandSender sender, Map<String, Account> accounts){
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
    }

    public static boolean adminDeposit(String[] args, CommandSender sender, Map<String, Account> accounts){
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

                Transaction result = null;

                if (holder != null && amount > 0) {
                    result = holder.deposit(amount);
                }

                if (result != null && result.resultType.equals(Transaction.ResultType.SUCCESS)) {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully deposited " + ChatColor.AQUA + result.amount + " diamonds" +
                            ChatColor.GREEN + " into " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Deposit Failed");
                }


            }
        } else
            sender.sendMessage(Permissions.invalidPerm);

        return true;
    }

    public static boolean adminWithdraw(String[] args, CommandSender sender, Map<String, Account> accounts){
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

                Transaction result = null;

                if (holder != null && amount > 0) {
                    result = holder.withdraw(amount,VaultType.ALL);
                }

                if (result != null && result.resultType.equals(Transaction.ResultType.SUCCESS)) {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully withdrew " + ChatColor.AQUA + result.amount + " diamonds" +
                            ChatColor.GREEN + " from " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Withdraw Failed");
                }
            }
        } else
            sender.sendMessage(Permissions.invalidPerm);

        return true;
    }

    public static boolean save(CommandSender sender){
        if(Util.isAdmin(sender)) {
            try {
                ItemEconomy.getInstance().saveData();
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully saved data");
            } catch (Exception e) {
                if (ItemEconomy.getInstance().isDebugMode())
                    e.printStackTrace();

                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "FAILED to save data...files corrupt");
            }
        } else{
            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You have no rights to send this command.");
        }

        return true;
    }

    public static boolean load(CommandSender sender){
        if(Util.isAdmin(sender)) {
            try {
                ItemEconomy.getInstance().loadData();
                Config.loadConfig();
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully loaded data");
            } catch (Exception e) {
                if (ItemEconomy.getInstance().isDebugMode())
                    e.printStackTrace();

                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "FAILED to load data...files corrupt");
            }
        } else{
            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You have no rights to send this command.");
        }

        return true;
    }


}
