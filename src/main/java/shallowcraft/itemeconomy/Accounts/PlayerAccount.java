package shallowcraft.itemeconomy.Accounts;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Vault.Vault;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerAccount implements Account {
    private final OfflinePlayer player;
    private List<Vault> vaults;
    private final Material itemCurrency;
    private int lastPersonalBalance;


    public PlayerAccount(OfflinePlayer player, Material itemCurrency){
        this.player = player;
        vaults = new ArrayList<>();
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = 0;
    }

    public PlayerAccount(OfflinePlayer player, Material itemCurrency, int personalBalance){
        this.player = player;
        vaults = new ArrayList<>();
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = personalBalance;
    }

    public PlayerAccount(OfflinePlayer player, Material itemCurrency, int personalBalance, List<Vault> vaults){
        this.player = player;
        this.vaults = new ArrayList<>(vaults);
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = personalBalance;
    }


    @Override
    public int getBalance() {
        return balance();
    }

    public int getLastPersonalBalance(){return lastPersonalBalance;}

    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public List<Vault> getVaults() {
        return new ArrayList<>(vaults);
    }

    @Override
    public void overrideLoadVaults(List<Vault> override){
        vaults = new ArrayList<>(override);
    }

    @Override
    public Material getItemCurrency(){
        return itemCurrency;
    }

    private boolean updatePersonalBalance(){
        Inventory inventory = null;

        try{
            inventory = player.getPlayer().getInventory();
        } catch (Exception ignored){
        }

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
        return Util.getAllVaultsBalance(vaults);
    }

    @Override
    public boolean removeVault(Vault vault){
        return vaults.remove(vault);
    }

    @Override
    public void addVault(Vault vault) {
        vaults.add(vault);
        ItemEconomy.getInstance().saveData();
    }

    @Override
    public TransactionResult withdraw(int amount){
        //ItemEconomy.log.info("Withdrawing " + amount + " from total of " + balance());
        if(balance() < amount)
            return new TransactionResult(0, ResultType.INSUFFICIENT_FUNDS, "withdraw");

        int removed = 0;

        TransactionResult result = Transaction.withdrawAllVaults(amount, balance(), vaults);
        removed += result.amount;


        if(result.type == ResultType.FAILURE){
            Inventory inventory =  null;
            try{
                inventory = player.getPlayer().getInventory();
            } catch (Exception ignore){
            }
            if(inventory != null){
                result = Transaction.withdraw(inventory, amount - removed);
                removed += result.amount;
            }
        }

        updatePersonalBalance();
        ItemEconomy.getInstance().saveData();
        return new TransactionResult(removed, result.type, "withdraw");
    }

    @Override
    public TransactionResult deposit(int amount){
        //ItemEconomy.log.info("Depositing " + amount + " into total of " + balance());



        int numAdded = 0;
        TransactionResult result = Transaction.depositAllVaults(amount, vaults);
        numAdded += result.amount;

        if(result.type == ResultType.FAILURE){
            Inventory inventory =  null;
            try{
                inventory = player.getPlayer().getInventory();
            } catch (Exception ignore){
            }

            if(inventory != null){
                result = Transaction.deposit(inventory, amount - numAdded);
                numAdded += result.amount;
            }
        }

        updatePersonalBalance();
        ItemEconomy.getInstance().saveData();
        return new TransactionResult(numAdded, result.type, "deposit");
    }

    @Override
    public String getID() {
        return player.getUniqueId().toString();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public int hashCode() {
        return UUID.fromString(getID()).hashCode();
    }
}
