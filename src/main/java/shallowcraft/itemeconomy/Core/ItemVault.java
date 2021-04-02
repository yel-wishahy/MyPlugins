package shallowcraft.itemeconomy.Core;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ListIterator;

public class ItemVault {
    public final Block containerVault;
    public final Sign vaultSign;
    public final Account holder;
    private Material itemCurrency;
    public final VaultType vaultType;


    public ItemVault(Block container, Sign vaultSign, Account holder, Material itemCurrency){
        this.containerVault = container;
        this.vaultSign = vaultSign;
        this.holder = holder;
        this.itemCurrency = itemCurrency;
        this.vaultType = VaultType.REGULAR;
    }

    public ItemVault(Block container, Sign vaultSign, Account holder, Material itemCurrency, VaultType vaultType){
        this.containerVault = container;
        this.vaultSign = vaultSign;
        this.holder = holder;
        this.itemCurrency = itemCurrency;
        this.vaultType = vaultType;
    }

    public int getVaultBalance(){
        if(Util.isValidVaultSign(vaultSign) && containerVault.equals(Util.chestBlock(vaultSign))){
            Inventory inventory =  ((Container) containerVault.getState()).getInventory();
            return Util.countItem(inventory);
        } else{
            destroy();
            return -1;
        }
    }

    public TransactionResult withdraw(int amount){
        Inventory inventory =  ((Container) containerVault.getState()).getInventory();
        return Transaction.withdraw(inventory, amount);
    }

    public TransactionResult deposit(int amount){
        Inventory inventory =  ((Container) containerVault.getState()).getInventory();
        return Transaction.deposit(inventory, amount);
    }

    private void destroy(){
        ItemEconomy.log.info("DESTROYING VAULT");
        holder.removeVault(this);
    }

    public static enum VaultType {
        REGULAR(1),
        DEPOSIT_ONLY(2),
        WITHDRAW_ONLY(3);

        private int id;

        VaultType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ItemVault.VaultType fromID(int id){
            switch (id){
                case 1:
                    return REGULAR;
                case 2:
                    return DEPOSIT_ONLY;
                case 3:
                    return WITHDRAW_ONLY;
                default:
                    return REGULAR;
            }
        }
    }

}
