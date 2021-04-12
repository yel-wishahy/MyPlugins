package shallowcraft.itemeconomy.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.Data.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Tax.Taxable;
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
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();
        if (Config.IECommandAliases.contains(label))
            return onIECommand(sender, command, label, args, accounts);
        else if (Config.TaxCommandAliases.contains(label))
            return onTaxCommand(sender,command,label,args,accounts);

        return false;
    }

    private static boolean onIECommand(CommandSender sender, Command command, String commandLabel, String[] args, Map<String, Account> accounts) {
        boolean isPlayer = isPlayer(sender);
        Player player = null;

        if (isPlayer)
            player = (Player) sender;

        if (args.length < 1)
            return false;

        switch (args[0]) {
            case "create_account":
                boolean addedAccount = false;

                if (args.length == 2) {
                    String name = args[1];
                    OfflinePlayer p = null;

                    if (!Util.isPlayerName(name) && sender.hasPermission(Permissions.adminPerm)) {
                        if (!accounts.containsKey(name)) {
                            accounts.put(name, new GeneralAccount(name));
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW Tax account ");
                        } else
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Tax account exists.");

                        return true;
                    }

                    try {
                        if (!accounts.containsKey(sender.getServer().getPlayerUniqueId(name).toString()))
                            p = sender.getServer().getPlayer(name);
                    } catch (Exception ignored) {
                    }

                    if (p != null) {
                        Account acc = new PlayerAccount(p, Config.currency);
                        accounts.put(acc.getID(), acc);
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account " +
                                ChatColor.AQUA + name + ChatColor.GREEN + " ! Lucky spending!");
                        addedAccount = true;
                    }
                }

                if (!addedAccount) {
                    if (isPlayer) {
                        if (!accounts.containsKey(player.getUniqueId().toString())) {
                            Account acc = new PlayerAccount(player, Config.currency);
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
                if (isPlayer) {
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
                if (isAdmin(sender)) {
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
                if (isAdmin(sender)) {
                    if (args.length == 3) {
                        Account holder = null;
                        int amount = 0;

                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception ignore) {
                        }

                        for (Account acc : accounts.values()) {
                            if (acc != null && acc.getName() != null) {
                                if (acc.getName().equals(args[1]))
                                    holder = acc;
                            }
                        }

                        TransactionResult result = null;

                        if (holder != null && amount > 0) {
                            result = holder.deposit(amount);
                        }

                        if (result != null && result.type.equals(ResultType.SUCCESS)) {
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
                if (isAdmin(sender)) {
                    if (args.length == 3) {
                        Account holder = null;
                        int amount = 0;

                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception ignore) {
                        }

                        for (Account acc : accounts.values()) {
                            if (acc != null && acc.getName() != null) {
                                if (acc.getName().equals(args[1]))
                                    holder = acc;
                            }
                        }

                        TransactionResult result = null;

                        if (holder != null && amount > 0) {
                            result = holder.withdraw(amount);
                        }

                        if (result != null && result.type.equals(ResultType.SUCCESS)) {
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

    private static boolean onTaxCommand(CommandSender sender, Command command, String commandLabel, String[] args, Map<String, Account> accounts) {
        boolean isPlayer = isPlayer(sender);
        Player player = null;

        if (isPlayer)
            player = (Player) sender;

        if (args.length < 1)
            return false;

        switch (args[0]) {
            case "add_tax":
                boolean success = false;
                if (args.length == 4 && sender.hasPermission(Permissions.adminPerm)) {
                    String playerName = args[1];
                    String taxName = args[2];
                    double taxRate = Double.parseDouble(args[3]);

                    if (accounts.containsKey(Util.getPlayerID(playerName)) && taxRate > 0 && taxRate <= Config.taxCap) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));
                        holder.addTax(new Taxable(holder, taxName, taxRate));

                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully added new tax: " + ChatColor.YELLOW + taxName + ChatColor.GREEN +
                                " with rate " + ChatColor.YELLOW + taxRate + ChatColor.GREEN + " to " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                        success = true;
                    }
                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command.");
                else if (!success)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return success;
            case "remove_tax":
                boolean pass = false;
                if (args.length == 3 && sender.hasPermission(Permissions.adminPerm)) {
                    String playerName = args[1];
                    String taxName = args[2];

                    if (accounts.containsKey(Util.getPlayerID(playerName))) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));

                        if (holder.getTaxes().containsKey(taxName)) {
                            holder.removeTax(taxName);
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully removed tax: " + ChatColor.YELLOW + taxName + ChatColor.GREEN +
                                    " from " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                            pass = true;
                        }
                    }
                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command.");
                else if (!pass)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass;
            case "clear_tax":
                boolean pass1 = false;
                if (args.length == 2 && sender.hasPermission(Permissions.adminPerm)) {
                    String playerName = args[1];

                    if (accounts.containsKey(Util.getPlayerID(playerName))) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));
                        holder.overrideLoadTaxes(new HashMap<>());

                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully removed all taxes from " +
                                ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                        pass1 = true;
                    }
                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command.");
                else if (!pass1)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass1;
            case "tax_info":
                boolean pass2 = false;
                if (args.length == 2 || (args.length == 3 && args[2].equals("all"))) {
                    String playerName = args[1];

                    if (accounts.containsKey(Util.getPlayerID(playerName))) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));
                        StringBuilder msg = new StringBuilder();
                        msg.append(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + "List of TAXES:\n");

                        for (Taxable tax : holder.getTaxes().values()) {
                            if (tax != null){
                                msg.append(ChatColor.GREEN + "* Tax Name: " + ChatColor.AQUA).append(tax.getTaxName()).append(ChatColor.GREEN).append(" Tax %: ").
                                        append(ChatColor.YELLOW).append(tax.getTaxRate()).append(ChatColor.GREEN).append(" Next Tax Time: ").append(ChatColor.YELLOW).
                                        append(Config.taxTimeFormat.format(tax.getNextTaxTime())).append("\n");
                            }

                        }
                        sender.sendMessage(msg.toString());
                        pass2 = true;
                    }
                } else if (args.length == 3){
                    String playerName = args[1];
                    String taxName = args[2];

                    if (accounts.containsKey(Util.getPlayerID(playerName))) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));

                        if(holder.getTaxes().containsKey(taxName)){
                            Taxable tax = holder.getTaxes().get(taxName);
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Tax Rate (%): " + ChatColor.YELLOW
                                    + tax.getTaxRate() + ChatColor.GREEN + " Next Tax Time: " + ChatColor.YELLOW + Config.taxTimeFormat.format(tax.getNextTaxTime()));
                            pass2 = true;
                        }
                    }

                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command.");
                else if (!pass2)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass2;
            case "tax_all":
                if(sender.hasPermission(Permissions.adminPerm)){
                    for (Account acc:accounts.values()) {
                        if(acc instanceof PlayerAccount) {
                            TransactionResult r = ((PlayerAccount) acc).taxAll();
                            TextComponent t = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "RBME Taxed " + ChatColor.AQUA + acc.getName() + " " +
                                    ChatColor.YELLOW + r.amount + ChatColor.AQUA + " Diamonds!");
                            sender.getServer().broadcast(t, Permissions.msgPerm);


                        }
                    }
                }
            case "tax":
                boolean pass3 = false;
                if (args.length == 2 || (args.length == 3 && args[2].equals("all"))) {
                    String playerName = args[1];

                    if (accounts.containsKey(Util.getPlayerID(playerName))) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));
                        TransactionResult r = holder.taxAll();
                        TextComponent t = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "RBME Taxed " + ChatColor.AQUA + holder.getName() + " " +
                                ChatColor.YELLOW + r.amount + ChatColor.AQUA + " Diamonds!");
                        sender.sendMessage(t);
                        pass3 = true;
                    }
                } else if (args.length == 3){
                    String playerName = args[1];
                    String taxName = args[2];

                    if (accounts.containsKey(Util.getPlayerID(playerName))) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));

                        if(holder.getTaxes().containsKey(taxName)){
                            Taxable tax = holder.getTaxes().get(taxName);
                            TransactionResult r = tax.tax();
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Tax Rate (%): " + ChatColor.YELLOW
                                    + tax.getTaxRate() + ChatColor.GREEN + " Next Tax Time: " + ChatColor.YELLOW + Config.taxTimeFormat.format(tax.getNextTaxTime()));
                            TextComponent t = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "RBME Taxed " + ChatColor.AQUA + holder.getName() + " " +
                                    ChatColor.YELLOW + r.amount + ChatColor.AQUA + " Diamonds!");
                            sender.sendMessage(t);
                            pass3 = true;
                        }
                    }

                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command.");
                else if (!pass3)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass3;

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
