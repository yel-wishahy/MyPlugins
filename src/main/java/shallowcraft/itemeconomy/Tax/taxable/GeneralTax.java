package shallowcraft.itemeconomy.Tax.taxable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.ChatColor;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Data.Permissions;
import shallowcraft.itemeconomy.*;
import shallowcraft.itemeconomy.Tax.Taxation;
import shallowcraft.itemeconomy.Transaction.TransactionResult;

import java.util.Date;

public class GeneralTax implements Taxable {
    private final Account holder;
    private String taxName;
    private double taxRate;
    private Date lastTaxTime;
    private Date nextTaxTime;


    public GeneralTax(Account holder, String name, double tax){
        this.holder = holder;
        this.taxRate = tax;
        this.taxName = name;
        setTaxTimes();
        ItemEconomy.log.info(this.toString());
    }

    public GeneralTax(Account holder, String name, double tax, Date lastTaxTime, Date nextTaxTime) {
        this.holder = holder;
        this.taxRate = tax;
        this.taxName = name;
        this.lastTaxTime = lastTaxTime;
        this.nextTaxTime = nextTaxTime;
        ItemEconomy.log.info(this.toString());
    }

    @Override
    public TransactionResult tax(){
        Date now = new Date();
        if(now.compareTo(nextTaxTime) > 0){
            Account deposit = Taxation.getTaxDeposit();
            if(deposit!=null){
                int taxable = amountToTax();
                TransactionResult withdrawResult = holder.forcedWithdraw(taxable);
                deposit.deposit(withdrawResult.amount);

                setTaxTimes();

                TextComponent announcement = Component.text(ChatColor.GREEN + "* Player: " + ChatColor.YELLOW + holder.getName() +
                        ChatColor.GREEN + " has been taxed " + ChatColor.YELLOW + withdrawResult.amount + ChatColor.AQUA + " Diamonds" + ChatColor.GREEN + " at a rate of " +
                        ChatColor.YELLOW + taxRate + " %" + ChatColor.GREEN + " as a result of the" + ChatColor.RED + " " + ChatColor.BOLD + taxName + " TAX \n");
                ItemEconomyPlugin.getInstance().getServer().broadcast(announcement, Permissions.msgPerm);
                return new TransactionResult(withdrawResult.amount, TransactionResult.ResultType.SUCCESS, "tax");
            }
        }

        return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "tax");
    }

    @Override
    public Date getLastTaxTime() {
        return lastTaxTime;
    }

    @Override
    public double getTaxRate(){
        return taxRate;
    }

    @Override
    public Date getNextTaxTime() {
        return nextTaxTime;
    }

    @Override
    public String getTaxName(){
        return taxName;
    }

    private int amountToTax(){
        int bal = holder.getChequingBalance();
        return (int) (bal * taxRate/100.0);
    }

    private void setTaxTimes(){
        this.lastTaxTime = new Date();
        this.nextTaxTime = DateUtils.addHours(lastTaxTime, Config.nextTaxHours);
    }

    @Override
    public void updateTaxTime(){
        this.nextTaxTime = new Date();
        ItemEconomy.getInstance().saveData();
    }

    @Override
    public void updateRate(double amount){
        taxRate = amount;
    }

    @Override
    public String toString() {
        return "GeneralTax{" +
                "holder=" + holder +
                ", taxName='" + taxName + '\'' +
                ", taxRate=" + taxRate +
                ", lastTaxTime=" + lastTaxTime +
                ", nextTaxTime=" + nextTaxTime +
                '}';
    }
}
