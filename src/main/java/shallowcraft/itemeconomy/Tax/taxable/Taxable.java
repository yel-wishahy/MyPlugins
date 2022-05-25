package shallowcraft.itemeconomy.Tax.taxable;

import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Transaction.Transaction;

import java.util.Date;

public interface Taxable {
    public Transaction tax();
    public Date getLastTaxTime();
    public double getTaxRate();
    public Date getNextTaxTime();
    public String getTaxName();
    public void updateTaxTime();
    public void updateRate(double amount);
    public Account getTaxDeposit();
}
