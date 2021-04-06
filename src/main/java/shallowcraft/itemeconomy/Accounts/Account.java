package shallowcraft.itemeconomy.Accounts;

import org.bukkit.Material;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Vault.Vault;

import java.util.List;

public interface Account {
    public int getBalance();
    public List<Vault> getVaults();
    public void overrideLoadVaults(List<Vault> override);
    public Material getItemCurrency();
    public boolean removeVault(Vault vault);
    public void addVault(Vault vault);
    public TransactionResult withdraw(int amount);
    public TransactionResult deposit(int amount);
    public String getID();
    public String getName();
    public int hashCode();
}