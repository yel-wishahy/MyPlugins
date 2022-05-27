package shallowcraft.itemeconomy.Accounts;

import shallowcraft.itemeconomy.Data.Serializable;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;
import shallowcraft.itemeconomy.Util.Util;

import java.util.List;

/**
 * interface for accounts, create an account class by implementing this interface
 */
public interface Account extends Serializable<Account> {

    public int getBalance(VaultType vaultType);
    public List<Vault> getVaults();
    public void overrideLoadVaults(List<Vault> override);
    public boolean removeVault(Vault vault);
    public void addVault(Vault vault);
    public Transaction withdraw(int amount, VaultType vaultType);
    public Transaction deposit(int amount);
    public String getID();
    public String getName();
    public int hashCode();
    public String getAccountType();
    public Transaction transfer(VaultType source, VaultType destination, int amount);
    public void transactionBalanceBuffer(double amount);
    public double getBalanceBuffer();
    public default Transaction convertBalanceBuffer(){
        Transaction result = Util.convertBalanceBuffer(this);
        return new Transaction(result.amount,result.resultType, result.errorMessage,this, Transaction.TransactionType.DEPOSIT);
    }
}
