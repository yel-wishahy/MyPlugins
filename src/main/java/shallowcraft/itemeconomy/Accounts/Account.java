package shallowcraft.itemeconomy.Accounts;

import shallowcraft.itemeconomy.Data.Serializable;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;

import java.util.List;

/**
 * interface for accounts, create an account class by implementing this interface
 */
public interface Account extends Serializable<Account> {

    public int getChequingBalance();
    public int getBalance();
    public int getBalance(VaultType vaultType);
    public List<Vault> getVaults();
    public void overrideLoadVaults(List<Vault> override);
    public boolean removeVault(Vault vault);
    public void addVault(Vault vault);
    public TransactionResult withdraw(int amount);
    public TransactionResult forcedWithdraw(int amount);
    public TransactionResult deposit(int amount);
    public String getID();
    public String getName();
    public int hashCode();
    public String getAccountType();
    public TransactionResult transfer(VaultType source, VaultType destination, int amount);
    public void updateBalanceBuffer(double amount);
    public double getBalanceBuffer();
    public TransactionResult convertBalanceBuffer();
}
