package shallowcraft.itemeconomy.Tax;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Permissions;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taxation {
    private static Taxation instance;
    @Getter private Account mainTaxDeposit;

    private Taxation(){
        mainTaxDeposit = getTaxDeposit();
    }

    public static Taxation getInstance(){
        if(instance == null){
            instance = new Taxation();
        }

        return instance;
    }


    public TransactionResult redistribute(Map<String, Account> accounts){
        int totalCirculation = Util.getTotalCirculation();
        TextComponent starting = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Starting daily wealth distribution. Total currency " +
                "in circulation right now is: " + ChatColor.YELLOW + totalCirculation + ChatColor.AQUA + " Diamonds.");
        ItemEconomyPlugin.getInstance().getServer().broadcast(starting, Permissions.playerPerm);

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
                    ItemEconomyPlugin.getInstance().getServer().broadcast(info, Permissions.playerPerm);
                } else {
                    savers.add((PlayerAccount) acc);
                }

            }
        }


        for (PlayerAccount hoarder:hoarders) {
            if(isHoarding(hoarder.getChequingBalance(), totalCirculation)){
                double percent = ((double) hoarder.getBalance())/((double)totalCirculation);
                percent-=Config.wealthCap/100.0;
                if(percent > 0 && percent <= 1){

                    int toTake = (int) ((percent) * ((double) hoarder.getBalance()));
                    TransactionResult result = hoarder.forcedWithdraw(toTake);
                    toDistribute += result.amount;

                    TextComponent hoarderInfo = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Embezzled " + ChatColor.YELLOW + result.amount + ChatColor.AQUA
                    + " diamonds " + ChatColor.GREEN + "from " + ChatColor.YELLOW + hoarder.getName() + "'s " + ChatColor.GREEN + " account!");
                    ItemEconomyPlugin.getInstance().getServer().broadcast(hoarderInfo, Permissions.playerPerm);
                }
            }
        }

        if(toDistribute > 0 && savers.size() > 0){
            int split = (int) (((double) toDistribute)/((double) savers.size()));
            ItemEconomy.log.info("split: " + split);
            int remainder = toDistribute - split * savers.size();
            tax(remainder);
            remainder = 0;

            for(PlayerAccount saver: savers){
                TransactionResult result = saver.deposit(split);
                remainder+=split-result.amount;
                distributed+=result.amount;

                TextComponent saverInfo = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "'Donated' " + ChatColor.YELLOW + result.amount + ChatColor.AQUA
                        + " diamonds " + ChatColor.GREEN + "to " + ChatColor.YELLOW + saver.getName() + "'s " + ChatColor.GREEN + " account! Your welcome. ;)");
                ItemEconomyPlugin.getInstance().getServer().broadcast(saverInfo, Permissions.playerPerm);
            }

            tax(remainder);
        }

        if(distributed == toDistribute)
            return new TransactionResult(distributed, TransactionResult.ResultType.SUCCESS, "distribution");

        return new TransactionResult(distributed, TransactionResult.ResultType.FAILURE, "distribution");
    }


    private boolean isHoarding(int balance, int circulation){
        return ((double) balance)/((double) circulation) > Config.wealthCap/100.0;
    }

    public TransactionResult taxAllProfits(Map<String, Account> accounts){
        int totalCirculation = Util.getTotalCirculation();
        TextComponent starting = Component.text(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Starting daily" + ChatColor.BOLD + " " +
                ChatColor.RED + "Income Tax!" + ChatColor.RESET + " " + ChatColor.GREEN + "Total currency " +
                "in circulation right now is: " + ChatColor.YELLOW + totalCirculation + ChatColor.AQUA + " Diamonds.");
        ItemEconomyPlugin.getInstance().getServer().broadcast(starting, Permissions.playerPerm);

        Map<String, Integer> taxables = getTaxableProfits();
        int totalTaxed = 0;

        for (String id:taxables.keySet()) {
            int taxable = taxables.get(id);
            PlayerAccount holder = (PlayerAccount) accounts.get(id);

            if (taxable >= 0) {
                int profit = Util.getProfits().get(id);
                double r = (((double) taxable)/profit * 100);
                if(profit< 1)
                    r = 0.0;
                String rate = (new DecimalFormat("#.##")).format(r);

                TransactionResult result = holder.forcedWithdraw(taxable);
                tax(result.amount);
                totalTaxed += result.amount;

                TextComponent announcement = Component.text(ChatColor.GREEN + "* Player: " + ChatColor.YELLOW + holder.getName() +
                        ChatColor.GREEN + " has been taxed " + ChatColor.YELLOW + result.amount + ChatColor.AQUA + " Diamonds" + ChatColor.GREEN + " at a rate of " +
                        ChatColor.YELLOW + rate + " %" + ChatColor.GREEN + " applied to an income of " +
                        ChatColor.YELLOW + profit + ChatColor.AQUA + " Diamonds \n");

                ItemEconomyPlugin.getInstance().getServer().broadcast(announcement, Permissions.playerPerm);
            }
        }

        Util.updateAllPlayerSavings();

        return new TransactionResult(totalTaxed, TransactionResult.ResultType.SUCCESS, "profit tax");
    }

    private int amountToTax(int profit, double rate){
        return (int) (profit * rate);
    }

    private double getProportionalRate(int profit, int maxProfit){
        double ratio = ((double) profit / (double) maxProfit);
        double rate = 0;

        if(ratio >= 1)
            rate = Config.maxProfitTax/100;
        else
            rate = ratio * Config.maxProfitTax/100;

        return rate;
    }

    public void resetSavings(){
        for (Account acc:ItemEconomy.getInstance().getAccounts().values()) {
            if(acc instanceof PlayerAccount){
                PlayerAccount holder = (PlayerAccount) acc;
                holder.updateSavings();
             }
        }
    }

    //gets amount to tax based on profits
    public Map<String, Integer> getTaxableProfits(){
        Map<String, Integer> outputTaxable = new HashMap<>();
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();
        Map<String, Integer> profits = Util.sortByValue(Util.getProfits());
        List<String> keys = new ArrayList<>(profits.keySet());

        String maxProfitID = keys.get(keys.size() - 1);
        int maxProfit = profits.get(maxProfitID);

        for (String id:keys) {
            int profit = profits.get(id);
            PlayerAccount holder = (PlayerAccount) accounts.get(id);

            if (profit >= Config.minimumProfit) {

                double rate = getProportionalRate(profit, maxProfit);
                int taxable = amountToTax(profit, rate);
                outputTaxable.put(holder.getID(), taxable);
            } else{
                outputTaxable.put(holder.getID(), 0);
            }
        }


        return outputTaxable;
    }

    public boolean tax(double amount) {
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof GeneralAccount && ((GeneralAccount) acc).isMainTaxDeposit) {
                GeneralAccount account = (GeneralAccount) acc;
                account.balanceBuffer += amount;
                return true;
            }
        }

        return false;
    }

    public boolean tax(double amount, Account taxDeposit) {
        if (taxDeposit instanceof GeneralAccount account && ((GeneralAccount) taxDeposit).isMainTaxDeposit) {
            account.balanceBuffer += amount;
            return true;
        }

        return false;
    }

    private Account getTaxDeposit() {
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof GeneralAccount && ((GeneralAccount) acc).isMainTaxDeposit) {
                return acc;
            }
        }

        Account deposit = new GeneralAccount(Config.taxID);
        ItemEconomy.getInstance().getAccounts().put(deposit.getID(),deposit);
        return deposit;
    }
}
