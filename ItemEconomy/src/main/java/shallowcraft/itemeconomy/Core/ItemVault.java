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


    public ItemVault(Block container, Sign vaultSign, Account holder, Material itemCurrency){
        this.containerVault = container;
        this.vaultSign = vaultSign;
        this.holder = holder;
        this.itemCurrency = itemCurrency;
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
        return Util.withdraw(inventory, amount);
    }

    public TransactionResult deposit(int amount){
        Inventory inventory =  ((Container) containerVault.getState()).getInventory();
        return Util.deposit(inventory, amount);
    }

    private void destroy(){
        holder.removeVault(this);
    }

}
