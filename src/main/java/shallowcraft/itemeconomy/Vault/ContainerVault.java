package shallowcraft.itemeconomy.Vault;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

public class ContainerVault implements Vault {
    public final Block containerVault;
    public final Sign vaultSign;
    public final Account holder;
    private Material itemCurrency;
    public final VaultType vaultType;


    public ContainerVault(Block container, Sign vaultSign, Account holder, Material itemCurrency){
        this.containerVault = container;
        this.vaultSign = vaultSign;
        this.holder = holder;
        this.itemCurrency = itemCurrency;
        this.vaultType = VaultType.REGULAR;
        setPDC(true);
    }

    public ContainerVault(Block container, Sign vaultSign, Account holder, Material itemCurrency, VaultType vaultType){
        this.containerVault = container;
        this.vaultSign = vaultSign;
        this.holder = holder;
        this.itemCurrency = itemCurrency;
        this.vaultType = vaultType;
        setPDC(true);
    }

    public void setPDC(boolean value){
        vaultSign.getPersistentDataContainer().set(new NamespacedKey(ItemEconomy.getInstance(), Config.PDCSignKey), PersistentDataType.STRING, String.valueOf(value));
        vaultSign.update();
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
    public TransactionResult withdraw(int amount){
        Inventory inventory =  ((Container) containerVault.getState()).getInventory();
        return Transaction.withdraw(inventory, amount);
    }

    @Override
    public TransactionResult deposit(int amount){
        Inventory inventory =  ((Container) containerVault.getState()).getInventory();
        return Transaction.deposit(inventory, amount);
    }

    @Override
    public void destroy(){
        setPDC(false);
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

    @Override
    public String toString() {
        return "ContainerVault{" +
                "containerVault=" + containerVault +
                ", vaultSign=" + vaultSign +
                ", holder=" + holder +
                ", itemCurrency=" + itemCurrency +
                ", vaultType=" + vaultType +
                '}';
    }
}
