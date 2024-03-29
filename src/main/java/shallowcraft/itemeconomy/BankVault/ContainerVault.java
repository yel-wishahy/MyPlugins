package shallowcraft.itemeconomy.BankVault;

import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.TransactionUtils;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Util.Util;

public class ContainerVault implements Vault {
    public final Block containerVault;
    public final Sign vaultSign;
    public final Account holder;
    public final VaultType vaultType;


    public ContainerVault(Block container, Sign vaultSign, Account holder){
        this.containerVault = container;
        this.vaultSign = vaultSign;
        this.holder = holder;
        this.vaultType = VaultType.REGULAR;
    }

    public ContainerVault(Block container, Sign vaultSign, Account holder,VaultType vaultType){
        this.containerVault = container;
        this.vaultSign = vaultSign;
        this.holder = holder;
        this.vaultType = vaultType;
    }

    @Override
    public int getVaultBalance(){
        if(checkVault()) {
            Inventory inventory = ((Container) containerVault.getState()).getInventory();
            return Util.countItem(inventory);
        }

        return -1;
    }

    @Override
    public boolean checkVault(){
        if(Util.isValidVaultSign((Sign) vaultSign.getBlock().getState()) && containerVault.equals(Util.chestBlock(vaultSign)))
            return true;
        else{
            destroy();
            return false;
        }
    }

    @Override
    public Transaction withdraw(int amount){
        Inventory inventory =  ((Container) containerVault.getState()).getInventory();
        return TransactionUtils.withdraw(inventory, amount);
    }

    @Override
    public Transaction deposit(int amount){
        Inventory inventory =  ((Container) containerVault.getState()).getInventory();
        return TransactionUtils.deposit(inventory, amount);
    }

    @Override
    public void destroy(){
        ItemEconomy.log.info("DESTROYING A VAULT");
        holder.removeVault(this);
    }

    @Override
    public VaultType getVaultType() {
        return vaultType;
    }

    @Override
    public Sign getSign() {
        return vaultSign;
    }

    @Override
    public Block getContainer() {
        return containerVault;
    }

    @Override
    public Account getHolder() {
        return holder;
    }
}
