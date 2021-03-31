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
    private final Material itemCurrency;
    private int lastPersonalBalance;


    public Account(OfflinePlayer player, Material itemCurrency){
        this.player = player;
        vaults = new ArrayList<>();
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = 0;
    }

    public Account(OfflinePlayer player, Material itemCurrency, int personalBalance){
        this.player = player;
        vaults = new ArrayList<>();
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = personalBalance;
    }

    public Account(OfflinePlayer player, Material itemCurrency, int personalBalance, List<ItemVault> vaults){
        this.player = player;
        this.vaults = new ArrayList<>(vaults);
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = personalBalance;
    }


    public int getBalance() {
        return balance();
    }

    public int getLastPersonalBalance(){return lastPersonalBalance;}

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

    private boolean updatePersonalBalance(){
        Inventory inventory = Objects.requireNonNull(player.getPlayer()).getInventory();
        if(inventory != null){
            lastPersonalBalance = Util.countItem(inventory);
            return true;
        }

        return false;
    }

    private int balance(){
        int count = 0;

        if(updatePersonalBalance())
            count+=lastPersonalBalance;

        return count + getAllVaultBalance();
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
    public void addVault(ItemVault vault) {
        vaults.add(vault);
        ItemEconomy.getInstance().saveData();
    }


    private TransactionResult withdrawAllVaults(int amount){
        if(getAllVaultBalance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        int numRemoved = 0;
        for (ItemVault vault:new ArrayList<>(vaults)) {
            if(numRemoved >= amount)
                break;
            int toRemove = Util.amountToRemove(vault.getVaultBalance(), amount - numRemoved);
            numRemoved += vault.withdraw(toRemove).amount;
        }

        if(numRemoved < amount)
            return new TransactionResult(numRemoved, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        return new TransactionResult(numRemoved, TransactionResult.ResultType.SUCCESS, "withdraw");
    }

   private TransactionResult depositAllVaults(int amount){
        int numAdded = 0;

        for (ItemVault vault:new ArrayList<>(vaults)) {
            if(numAdded >= amount)
                break;

            numAdded+=vault.deposit(amount - numAdded).amount;
        }

        if(numAdded < amount)
            return new TransactionResult(numAdded, TransactionResult.ResultType.INSUFFICIENT_SPACE, "deposit");

        return new TransactionResult(numAdded, TransactionResult.ResultType.SUCCESS, "deposit");
    }

    public TransactionResult withdraw(int amount){
        //ItemEconomy.log.info("Withdrawing " + amount + " from total of " + balance());
        if(balance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        Inventory inventory =  Objects.requireNonNull(player.getPlayer()).getInventory();
        int toRemove = 0;

        if(inventory != null){
            toRemove = Util.amountToRemove(Util.countItem(inventory), amount);
            toRemove = Transaction.withdraw(inventory, toRemove).amount;
        }

        updatePersonalBalance();
        return withdrawAllVaults(amount - toRemove);
    }

    public TransactionResult deposit(int amount){
        //ItemEconomy.log.info("Depositing " + amount + " into total of " + balance());

        Inventory inventory =  Objects.requireNonNull(player.getPlayer()).getInventory();
        int numAdded = 0;

        if(inventory != null){
            TransactionResult playerResult = Transaction.deposit(inventory, amount);
            numAdded += playerResult.amount;
        }

        updatePersonalBalance();
        return depositAllVaults(amount - numAdded);
    }

}
