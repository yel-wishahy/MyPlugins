package shallowcraft.itemeconomy.Accounts;

import org.bukkit.Material;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Vault.Vault;
import shallowcraft.itemeconomy.Vault.VaultType;

import java.util.List;

public interface Account {
    public int getChequingBalance();
    public int getBalance();
    public int getBalance(VaultType vaultType);
    public List<Vault> getVaults();
    public void overrideLoadVaults(List<Vault> override);
    public Material getItemCurrency();
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
}
