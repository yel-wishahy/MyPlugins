package shallowcraft.itemeconomy;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Account {
    private final OfflinePlayer player;
    private List<ItemVault> vaults;
    private Material itemCurrency;


    public Account(OfflinePlayer player, Material itemCurrency){
        this.player = player;
        vaults = new ArrayList<>();
        this.itemCurrency = itemCurrency;
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

    public void overrideLoadVaults(List<ItemVault> override){
        vaults = new ArrayList<>(override);
    }

    public Material getItemCurrency(){
        return itemCurrency;
    }

    private int balance(){
        int count = 0;
        count+=Util.countItem(Objects.requireNonNull(player.getPlayer()).getInventory(), itemCurrency);
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
