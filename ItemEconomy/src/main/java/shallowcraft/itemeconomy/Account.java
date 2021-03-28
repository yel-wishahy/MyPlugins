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
        vaults = new ArrayList<>();
        personalInventory = player.getPlayer().getInventory();
    }

    public int getBalance() {
        return balance();
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public List<ItemVault> getVaults() {
        return new ArrayList<>(vaults);
    }

    private int balance(){
        int count = 0;
        ItemEconomy.log.info("checking player inv");
        count+=Util.countItem(personalInventory, itemCurrency);
        ItemEconomy.log.info("checking vaults, total vaults to check: " + vaults.size());
        for (ItemVault vault:new ArrayList<>(vaults)) {
            int current = vault.getVaultBalance();
            if(current > 0)
                count+=current;
        }

        return count;
    }

    public boolean removeVault(ItemVault vault){
        return vaults.remove(vault);
    }

    public boolean addVault(ItemVault vault){return vaults.add(vault);}
}
