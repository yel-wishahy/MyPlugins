package shallowcraft.itemeconomy;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private final OfflinePlayer player;
    private int balance;
    private List<ItemVault> vaults;
    private Inventory personalInventory;
    private Material itemCurrency = ItemEconomy.currency;


    public Account(OfflinePlayer player){
        balance = 0;
        this.player = player;
        vaults = new ArrayList<ItemVault>();
        personalInventory = player.getPlayer().getInventory();
    }

    public int getBalance() {
        return balance();
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public List<ItemVault> getVaults() {
        return vaults;
    }

    private int balance(){
        int count = 0;
        count+=Util.countItem(personalInventory, itemCurrency);
        for (ItemVault vault:vaults) {
            int current = vault.getVaultBalance();
            if(current > 0)
                count+=current;
        }

        return count;
    }

    public boolean removeVault(ItemVault vault){
        return vaults.remove(vault);
    }
}
