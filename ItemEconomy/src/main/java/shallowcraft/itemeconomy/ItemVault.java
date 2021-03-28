package shallowcraft.itemeconomy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
            return Util.countItem(inventory, itemCurrency);
        } else{
            destroy();
            return -1;
        }

    }

    private void destroy(){
        holder.removeVault(this);
    }

}
