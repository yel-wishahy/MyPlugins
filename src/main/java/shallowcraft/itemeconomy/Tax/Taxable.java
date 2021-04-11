package shallowcraft.itemeconomy.Tax;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.TransactionResult;

import java.text.DateFormat;
import java.util.Date;

public class Taxable {
    private final Account holder;
    private final String taxName;
    private final double taxRate;
    private Date lastTaxTime;
    private Date nextTaxTime;


    public Taxable(Account holder, String name, double tax){
        this.holder = holder;
        this.taxRate = tax;
        this.taxName = name;
        setTaxTimes();
    }

    public Taxable(Account holder, String name, double tax, Date lastTaxTime, Date nextTaxTime) {
        this.holder = holder;
        this.taxRate = tax;
        this.taxName = name;
        this.lastTaxTime = lastTaxTime;
        this.nextTaxTime = nextTaxTime;
    }

    public TransactionResult tax(){
        Date now = new Date();
        if(now.compareTo(nextTaxTime) > 0){
            Account deposit = ItemEconomy.getInstance().getTaxDeposit();
            if(deposit!=null){
                int taxable = amountToTax();
                TransactionResult withdrawResult = holder.withdraw(taxable);
                deposit.deposit(withdrawResult.amount);

                setTaxTimes();

                return new TransactionResult(withdrawResult.amount, ResultType.SUCCESS, "tax");
            }
        }

        return new TransactionResult(0, ResultType.FAILURE, "tax");
    }

    public String getLastTaxTime() {
        return Config.taxTimeFormat.format(lastTaxTime);
    }

    public double getTaxRate(){
        return taxRate;
    }

    public String getNextTaxTime() {
        return Config.taxTimeFormat.format(nextTaxTime);
    }

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

}
