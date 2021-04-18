package shallowcraft.itemeconomy.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
import shallowcraft.itemeconomy.Tax.Taxable;
import shallowcraft.itemeconomy.Tax.Taxation;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.Data.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Tax.GeneralTax;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.Vault.Vault;
import shallowcraft.itemeconomy.Vault.VaultType;

import java.text.DecimalFormat;
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
            case "transfer":
                TransactionResult r = new TransactionResult(0, ResultType.FAILURE, "transfer");
                if (args.length == 4) {
                    if(Util.isPlayerName(sender.getName())){
                        String id = Util.getPlayerID(sender.getName());
                        Account holder = accounts.get(id);
                        VaultType source = Util.getVaultTypeFromArgs(args[1]);
                        VaultType destination = Util.getVaultTypeFromArgs(args[2]);
                        int amount = Integer.parseInt(args[3]);

                        if(holder != null && amount > 0 && source != destination && holder.getBalance(source) >= amount){
                            r = holder.transfer(source, destination, amount);

                            if(holder.getChequingBalance() <= amount && ResultType.failureModes.contains(r.type))
                                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Encountered insufficient funds in specified vault type");
                        }
                    }
                }

                if(r.type == ResultType.SUCCESS){
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You successfully transferred " + ChatColor.YELLOW + r.amount +
                            ChatColor.AQUA + " diamonds " + ChatColor.GREEN + " between accounts!");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Transfer failed.");
                }

                return true;
            case "admintransfer":
                TransactionResult result1 = new TransactionResult(0, ResultType.FAILURE, "transfer");

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

                        if(holder.getChequingBalance() <= amount && ResultType.failureModes.contains(result1.type))
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Encountered insufficient funds in specified vault type");
                    }
                }

                if(result1.type == ResultType.SUCCESS){
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
                        Account acc = new PlayerAccount(p, Config.currency);
                        accounts.put(acc.getID(), acc);
                        sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account for " +
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
                if(args.length == 1 && isPlayer){
                    if (accounts.containsKey(player.getUniqueId().toString())) {
                        Account holder = accounts.get(player.getUniqueId().toString());
                        List<Vault> vaults =  holder.getVaults();
                        int deposit = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.DEPOSIT_ONLY, vaults));
                        int withdraw = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.WITHDRAW_ONLY, vaults));
                        int regular = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.REGULAR, vaults));

                        String rateOfChange = " ";
                        if(holder instanceof PlayerAccount) {
                            rateOfChange = Util.getPercentChangeMessage((PlayerAccount) holder);
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
                           rateOfChange = Util.getPercentChangeMessage((PlayerAccount) holder);
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
                baltopMessage.append(ChatColor.GOLD).append("[ItemEconomy] ").append(ChatColor.GREEN).append("Total Currency in Circulation:")
                        .append(ChatColor.YELLOW).append(" ").append(Util.getTotalCirculation()).append(ChatColor.AQUA).append(" Diamonds\n \n")
                        .append(ChatColor.GREEN).append("Global Player Holdings: \n");
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
                        String rateofchange = " ";

                        if(Util.isPlayerName(name)) {
                            PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(name));
                            rateofchange =  Util.getPercentChangeMessage(holder);
                        }

                        baltopMessage.append(j).append(". ").append(ChatColor.GOLD).append(name).append(" ".repeat(20 - name.length()));
                        baltopMessage.append(ChatColor.YELLOW).append(bals.get(name)).append(ChatColor.AQUA).append(" ").append(Config.currency.name().toLowerCase()).
                                append(rateofchange).append("\n");
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
                                if (acc.getName().equals(args[1]) || acc.getID().equals(args[1]))
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
                    sender.sendMessage(Permissions.invalidPerm);

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
                                if (acc.getName().equals(args[1]) || acc.getID().equals(args[1]))
                                    holder = acc;
                            }
                        }

                        TransactionResult result = null;

                        if (holder != null && amount > 0) {
                            result = holder.forcedWithdraw(amount);
                        }

                        if (result != null && result.type.equals(ResultType.SUCCESS)) {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully withdrew " + ChatColor.AQUA + result.amount + " diamonds" +
                                    ChatColor.GREEN + " from " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Withdraw Failed");
                        }
                    }
                } else
                    sender.sendMessage(Permissions.invalidPerm);

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
            case "resetprofits":
                if(sender.hasPermission(Permissions.adminPerm))
                    Taxation.resetSavings();
                else
                    sender.sendMessage(Permissions.invalidPerm);
                return true;
            case "taxprofits":
                if(sender.hasPermission(Permissions.adminPerm))
                    Taxation.taxAllProfits(accounts);
                else
                    sender.sendMessage(Permissions.invalidPerm);
                return true;
            case "redistribute":
                if(sender.hasPermission(Permissions.adminPerm))
                    Taxation.redistribute(accounts);
                else
                    sender.sendMessage(Permissions.invalidPerm);
                return true;
            case "add":
                boolean success = false;
                if (args.length == 4 && sender.hasPermission(Permissions.adminPerm)) {
                    String playerName = args[1];
                    String taxName = args[2];
                    double taxRate = Double.parseDouble(args[3]);

                    if (accounts.containsKey(Util.getPlayerID(playerName)) && taxRate > 0) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));

                        if(Util.totalTaxRate(holder) + taxRate <= Config.taxCap){
                            holder.addTax(new GeneralTax(holder, taxName, taxRate));

                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Successfully added new tax: " + ChatColor.YELLOW + taxName + ChatColor.GREEN +
                                    " with rate " + ChatColor.YELLOW + taxRate + ChatColor.GREEN + " to " + ChatColor.AQUA + holder.getName() + "'s" + ChatColor.GREEN + " account!");
                            success = true;
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED
                                    + "Total player tax rate cannot exceed + " + ChatColor.YELLOW + Config.taxCap + " %!!");
                        }

                    }
                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot send this command.");
                else if (!success)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return success;
            case "remove":
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
                    sender.sendMessage(Permissions.invalidPerm);
                else if (!pass)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass;
            case "clear":
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
                    sender.sendMessage(Permissions.invalidPerm);
                else if (!pass1)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass1;
            case "info":
                boolean pass2 = false;
                if (args.length == 2 || (args.length == 3 && args[2].equals("all"))) {
                    String playerName = args[1];

                    if (accounts.containsKey(Util.getPlayerID(playerName))) {
                        PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));
                        sender.sendMessage(Util.getTaxInfo(holder));
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

                else if (!pass2)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass2;
            case "taxall":
                if(sender.hasPermission(Permissions.adminPerm)){
                    for (Account acc:accounts.values()) {
                        if(acc instanceof PlayerAccount) {
                            TransactionResult r = ((PlayerAccount) acc).taxAll();
                        }
                    }
                } else
                    sender.sendMessage(Permissions.invalidPerm);
            case "tax":
                boolean pass3 = false;
                if(sender.hasPermission(Permissions.adminPerm)){
                    if (args.length == 2 || (args.length == 3 && args[2].equals("all"))) {
                        String playerName = args[1];

                        if (accounts.containsKey(Util.getPlayerID(playerName))) {
                            PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));
                            TransactionResult r = holder.taxAll();
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
                                pass3 = true;
                            }
                        }

                    }
                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(Permissions.invalidPerm);
                else if (!pass3)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass3;

            case "edit":
                boolean pass4 = false;
                Taxable tax = null;
                if(sender.hasPermission(Permissions.adminPerm)){
                    if(args.length == 4 && args[3].equals("timeset_now")){
                        String playerName = args[1];

                        if(args[2].equals("all")){
                            if (accounts.containsKey(Util.getPlayerID(playerName))) {
                                PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));

                                for (Taxable t:holder.getTaxes().values()) {
                                    t.updateTaxTime();
                                }
                            }


                        } else{
                            String taxName = args[2];

                            if (accounts.containsKey(Util.getPlayerID(playerName))) {
                                PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));

                                if(holder.getTaxes().containsKey(taxName)){
                                    tax = holder.getTaxes().get(taxName);
                                    tax.updateTaxTime();
                                    pass4 = true;
                                }
                            }
                        }

                    } else if(args.length == 5 && args[3].equals("set_rate")){
                        double taxRate = Double.parseDouble(args[4]);
                        String playerName = args[1];
                        String taxName = args[2];

                        if (accounts.containsKey(Util.getPlayerID(playerName))) {
                            PlayerAccount holder = (PlayerAccount) accounts.get(Util.getPlayerID(playerName));

                            if(holder.getTaxes().containsKey(taxName)){
                                tax = holder.getTaxes().get(taxName);

                                if(Util.totalTaxRate(holder) + taxRate - tax.getTaxRate() <= Config.taxCap){
                                    tax.updateRate(taxRate);
                                    pass4 = true;
                                } else
                                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED
                                            + "Total player tax rate cannot exceed + " + ChatColor.YELLOW + Config.taxCap + " %!!");
                            }
                        }
                    }
                }


                if(pass4){
                    StringBuilder msg = new StringBuilder();
                    msg.append(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + "Tax credentials updated, new info:\n");
                    msg.append(ChatColor.GREEN + "* Tax Name: " + ChatColor.AQUA).append(tax.getTaxName()).append(ChatColor.GREEN).append(" Tax %: ").
                            append(ChatColor.YELLOW).append(tax.getTaxRate()).append(ChatColor.GREEN).append(" Next Tax Time: ").append(ChatColor.YELLOW).
                            append(Config.taxTimeFormat.format(tax.getNextTaxTime())).append("\n");
                    sender.sendMessage(msg.toString());
                }

                if (!sender.hasPermission(Permissions.adminPerm))
                    sender.sendMessage(Permissions.invalidPerm);
                else if (!pass4)
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Invalid command format");

                return pass4;
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