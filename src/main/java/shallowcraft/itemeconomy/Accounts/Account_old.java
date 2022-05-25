package shallowcraft.itemeconomy.Accounts;

import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;
import shallowcraft.itemeconomy.Data.Serializable;
import shallowcraft.itemeconomy.Transaction.Transaction;

import java.util.List;

/**
 * interface for accounts, create an account class by implementing this interface
 */
public interface Account_old extends Serializable<Account_old> {

    public int getChequingBalance();
    public int getBalance();
    public int getBalance(VaultType vaultType);
    public List<Vault> getVaults();
    public void overrideLoadVaults(List<Vault> override);
    public boolean removeVault(Vault vault);
    public void addVault(Vault vault);
    public Transaction withdraw(int amount);
    public Transaction forcedWithdraw(int amount);
    public Transaction deposit(int amount);
    public String getID();
    public String getName();
    public int hashCode();
    public String getAccountType();
    public Transaction transfer(VaultType source, VaultType destination, int amount);
    public void updateBalanceBuffer(double amount);
    public double getBalanceBuffer();
}
