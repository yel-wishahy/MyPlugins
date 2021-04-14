package shallowcraft.itemeconomy.Tax;

import shallowcraft.itemeconomy.Transaction.TransactionResult;

import java.util.Date;

public interface Taxable {
    public TransactionResult tax();
    public Date getLastTaxTime();
    public double getTaxRate();
    public Date getNextTaxTime();
    public String getTaxName();
    public void updateTaxTime();
    public void updateRate(double amount);
}
