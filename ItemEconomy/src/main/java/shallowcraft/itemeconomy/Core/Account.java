package shallowcraft.itemeconomy.Core;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Util.Util;

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
        count+= Util.countItem(Objects.requireNonNull(player.getPlayer()).getInventory());
        ItemEconomy.log.info("checking vaults, total vaults to check: " + vaults.size());
        for (ItemVault vault:new ArrayList<>(vaults)) {
            int current = vault.getVaultBalance();
            if(current > 0)
                count+=current;
        }

        return count;
    }

    private int getAllVaultBalance(){
        int count = 0;
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

    private TransactionResult withdrawAllVaults(int amount){
        if(getAllVaultBalance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        TransactionResult result = null;
        for (ItemVault vault:new ArrayList<>(vaults)) {
            int toRemove = Util.amountToRemove(vault.getVaultBalance(), amount);
            result = vault.withdraw(toRemove);
        }

        return result;
    }

   private TransactionResult depositAllVaults(int amount){
        int numAdded = 0;
        TransactionResult result = null;
        for (ItemVault vault:new ArrayList<>(vaults)) {
            if(numAdded >= amount)
                break;

            result = vault.deposit(amount);
            numAdded+=result.amount;
        }

        if(numAdded >= amount)
            return new TransactionResult(numAdded, TransactionResult.ResultType.SUCCESS, "deposit");

        return new TransactionResult(numAdded, TransactionResult.ResultType.INSUFFICIENT_SPACE, "deposit");
    }

    public TransactionResult withdraw(int amount){
        ItemEconomy.log.info("Withdrawing " + amount + " from total of " + balance());
        if(balance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");


        Inventory inventory =  Objects.requireNonNull(player.getPlayer()).getInventory();
        int toRemove = Util.amountToRemove(Util.countItem(inventory), amount);
        Util.withdraw(inventory, toRemove);

        return withdrawAllVaults(amount - toRemove);
    }

    public TransactionResult deposit(int amount){
        ItemEconomy.log.info("Depositing " + amount + " into total of " + balance());
        int numAdded = 0;
        Inventory inventory =  Objects.requireNonNull(player.getPlayer()).getInventory();

        TransactionResult playerResult = Util.deposit(inventory, amount);
        numAdded += playerResult.amount;

        return depositAllVaults(amount - numAdded);
    }

}
