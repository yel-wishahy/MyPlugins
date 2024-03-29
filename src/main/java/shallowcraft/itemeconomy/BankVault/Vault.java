package shallowcraft.itemeconomy.BankVault;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Transaction.Transaction;

//vault interface to implement vault types
//not sure if this is neccessary but its ok
public interface Vault {
    public int getVaultBalance();
    public boolean checkVault();
    public Transaction withdraw(int amount);
    public Transaction deposit(int amount);
    public void destroy();
    public VaultType getVaultType();
    public Sign getSign();
    public Block getContainer();
    public Account getHolder();
}
