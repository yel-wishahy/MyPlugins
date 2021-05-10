package shallowcraft.itemeconomy.Tax.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Data.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Tax.taxable.GeneralTax;
import shallowcraft.itemeconomy.Tax.taxable.Taxable;
import shallowcraft.itemeconomy.Tax.Taxation;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

import java.util.HashMap;
import java.util.Map;

public class TaxCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        boolean isPlayer = Util.isPlayer(sender);
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();
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
                if(args.length == 1 && Util.isPlayerName(sender.getName())){
                    String playerID = Util.getPlayerID(sender.getName());

                    if(accounts.containsKey(playerID)){
                        PlayerAccount holder = (PlayerAccount) accounts.get(playerID);
                        sender.sendMessage(Util.getTaxInfo(holder));
                        pass2 = true;
                    }

                }
                else if (args.length == 2 || (args.length == 3 && args[2].equals("all"))) {
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
                                    + tax.getTaxRate() + ChatColor.GREEN + " Next Tax Time: " + ChatColor.YELLOW + Config.timeFormat.format(tax.getNextTaxTime()));
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
                            append(Config.timeFormat.format(tax.getNextTaxTime())).append("\n");
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
}
