package shallowcraft.itemeconomy.Tax;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.Data.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taxation {
    public static TransactionResult redistribute(Map<String, Account> accounts){
        int totalCirculation = Util.getTotalCirculation();
        TextComponent starting = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Starting daily wealth distribution. Total currency " +
                "in circulation right now is: " + ChatColor.YELLOW + totalCirculation + ChatColor.AQUA + " Diamonds.");
        ItemEconomy.getInstance().getServer().broadcast(starting, Permissions.playerPerm);

        List<PlayerAccount> hoarders = new ArrayList<>();
        List<PlayerAccount> savers = new ArrayList<>();

        int toDistribute = 0;
        int distributed = 0;


        for (Account acc:accounts.values()) {
            if(acc instanceof PlayerAccount){
                if (isHoarding(acc.getBalance(), totalCirculation)){
                    hoarders.add((PlayerAccount) acc);
                    double percent = ((double) acc.getBalance())/((double)totalCirculation);
                    TextComponent info = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Player " + ChatColor.YELLOW + acc.getName() + ChatColor.GREEN
                            + " has " + ChatColor.YELLOW + (percent * 100) + " %" + ChatColor.GREEN + " of the total currency..." + ChatColor.BOLD + " " + ChatColor.RED + "IMMEDIATE REDUCTION IS REQUIRED.");
                    ItemEconomy.getInstance().getServer().broadcast(info, Permissions.playerPerm);
                } else {
                    savers.add((PlayerAccount) acc);
                }

            }
        }


        for (PlayerAccount hoarder:hoarders) {
            if(isHoarding(hoarder.getBalance(), totalCirculation)){
                double percent = ((double) hoarder.getBalance())/((double)totalCirculation);
                percent-=Config.wealthCap;
                if(percent > 0 && percent <= 1){

                    int toTake = (int) ((percent) * ((double) hoarder.getBalance()));
                    TransactionResult result = hoarder.forcedWithdraw(toTake);
                    toDistribute += result.amount;

                    TextComponent hoarderInfo = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Embezzled " + ChatColor.YELLOW + result.amount + ChatColor.AQUA
                    + " diamonds " + ChatColor.GREEN + "from " + ChatColor.YELLOW + hoarder.getName() + "'s " + ChatColor.GREEN + " account!");
                    ItemEconomy.getInstance().getServer().broadcast(hoarderInfo, Permissions.playerPerm);
                }
            }
        }

        if(toDistribute > 0 && savers.size() > 0){
            int split = (int) (((double) toDistribute)/((double) savers.size()));
            ItemEconomy.log.info("split: " + split);
            int remainder = toDistribute - split * savers.size();
            ItemEconomy.getInstance().tax(remainder);
            remainder = 0;

            for(PlayerAccount saver: savers){
                TransactionResult result = saver.deposit(split);
                remainder+=split-result.amount;
                distributed+=result.amount;

                TextComponent saverInfo = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Embezzled " + ChatColor.YELLOW + result.amount + ChatColor.AQUA
                        + " diamonds " + ChatColor.GREEN + "into " + ChatColor.YELLOW + saver.getName() + "'s " + ChatColor.GREEN + " account!");
                ItemEconomy.getInstance().getServer().broadcast(saverInfo, Permissions.playerPerm);
            }

            ItemEconomy.getInstance().tax(remainder);
        }

        if(distributed == toDistribute)
            return new TransactionResult(distributed, ResultType.SUCCESS, "distribution");

        return new TransactionResult(distributed, ResultType.FAILURE, "distribution");
    }


    private static boolean isHoarding(int balance, int circulation){
        return ((double) balance)/((double) circulation) > Config.wealthCap;
    }

    public static TransactionResult taxAllProfits(Map<String, Account> accounts){
        int totalCirculation = Util.getTotalCirculation();
        TextComponent starting = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Starting daily" + ChatColor.BOLD + " " +
                ChatColor.RED + "Income Tax!" + ChatColor.RESET + " " + ChatColor.GREEN + "Total currency " +
                "in circulation right now is: " + ChatColor.YELLOW + totalCirculation + ChatColor.AQUA + " Diamonds.");
        ItemEconomy.getInstance().getServer().broadcast(starting, Permissions.playerPerm);

        int totalTaxed = 0;
        Map<String, Integer> profits = Util.sortByValue(Util.getProfits());
        List<String> keys = new ArrayList<>(profits.keySet());

        String maxProfitID = keys.get(keys.size() - 1);
        int maxProfit = profits.get(maxProfitID);

        if(maxProfit < Config.minimumProfit)
            return new TransactionResult(0, ResultType.SUCCESS, "insufficient profits");

        for (String id:keys) {
            int profit = profits.get(id);
            if (profit >= Config.minimumProfit) {
                PlayerAccount holder = (PlayerAccount) accounts.get(id);

                double rate = getProportionalRate(profit, maxProfit);
                int taxable = amountToTax(profit, rate);

                TransactionResult result = holder.forcedWithdraw(taxable);
                ItemEconomy.getInstance().tax(result.amount);
                totalTaxed += result.amount;

                TextComponent announcement = Component.text(ChatColor.GREEN + "* Player: " + ChatColor.YELLOW + holder.getName() +
                        ChatColor.GREEN + " has been taxed " + ChatColor.YELLOW + result.amount + ChatColor.AQUA + " Diamonds" + ChatColor.GREEN + " at a rate of " +
                        ChatColor.YELLOW + (rate * 100) + " %" + ChatColor.GREEN + " applied to an income of " + ChatColor.YELLOW + profit + ChatColor.AQUA + " Diamonds \n");

                ItemEconomy.getInstance().getServer().broadcast(announcement, Permissions.playerPerm);
            }
        }

        return new TransactionResult(totalTaxed, ResultType.SUCCESS, "profit tax");
    }

    private static int amountToTax(int profit, double rate){
        return (int) (profit * rate);
    }

    private static double getProportionalRate(int profit, int maxProfit){
        double ratio = ((double) profit / (double) maxProfit);
        double rate = 0;

        if(ratio >= 1)
            rate = Config.maxProfitTax;
        else
            rate = ratio * Config.maxProfitTax;

        return rate;
    }

    public static void resetSavings(){
        for (Account acc:ItemEconomy.getInstance().getAccounts().values()) {
            if(acc instanceof PlayerAccount){
                PlayerAccount holder = (PlayerAccount) acc;
                holder.updateSavings();
             }
        }
    }
}
