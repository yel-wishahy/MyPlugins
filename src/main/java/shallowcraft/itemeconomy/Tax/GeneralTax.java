package shallowcraft.itemeconomy.Tax;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.TransactionResult;

import java.text.DateFormat;
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
            Account deposit = ItemEconomy.getInstance().getTaxDeposit();
            if(deposit!=null){
                int taxable = amountToTax();
                TransactionResult withdrawResult = holder.forcedWithdraw(taxable);
                deposit.deposit(withdrawResult.amount);

                setTaxTimes();

                return new TransactionResult(withdrawResult.amount, ResultType.SUCCESS, "tax");
            }
        }

        return new TransactionResult(0, ResultType.FAILURE, "tax");
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
        int bal = holder.getBalance();
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
                "holder=" + holder.getName() +
                ", taxName='" + taxName + '\'' +
                ", taxRate=" + taxRate +
                ", lastTaxTime=" + lastTaxTime.toString() +
                ", nextTaxTime=" + nextTaxTime.toString() +
                '}';
    }
}
