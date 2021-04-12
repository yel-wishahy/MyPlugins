package shallowcraft.itemeconomy.Accounts;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Tax.Taxable;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Vault.Vault;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

import java.util.*;

public class PlayerAccount implements Account {
    private final OfflinePlayer player;
    private List<Vault> vaults;
    private Map<String, Taxable> taxes;
    private final Material itemCurrency;
    private int lastPersonalBalance;


    public PlayerAccount(OfflinePlayer player, Material itemCurrency){
        this.player = player;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = 0;
    }

    public PlayerAccount(OfflinePlayer player, Material itemCurrency, int personalBalance){
        this.player = player;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        this.itemCurrency = itemCurrency;
        lastPersonalBalance = personalBalance;
    }

    public PlayerAccount(OfflinePlayer player, Material itemCurrency, int personalBalance, List<Vault> vaults){
        this.player = player;
        this.vaults = new ArrayList<>(vaults);
        taxes = new HashMap<>();
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

    public HashMap<String, Taxable> getTaxes(){return new HashMap<>(taxes);}

    public void addTax(Taxable tax){
        taxes.put(tax.getTaxName(), tax);
        ItemEconomy.getInstance().saveData();
    }

    public void removeTax(Taxable tax){
        if(taxes.containsValue(tax))
            taxes.remove(tax.getTaxName());
    }

    public void removeTax(String tax){
        taxes.remove(tax);
    }

    @Override
    public void overrideLoadVaults(List<Vault> override){
        this.vaults = new ArrayList<>(override);
    }

    public void overrideLoadTaxes(Map<String, Taxable> override){
        this.taxes = new HashMap<>(override);
    }

    public TransactionResult taxAll(){
        int count = 0;
        for (Taxable tax: taxes.values()) {
            count += tax.tax().amount;
        }

        return new TransactionResult(count, ResultType.SUCCESS, "tax all");
    }

    @Override
    public Material getItemCurrency(){
        return itemCurrency;
    }

    private boolean updatePersonalBalance(){
        Inventory inventory = Util.getInventory(player);

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
        if(balance() < amount)
            return new TransactionResult(0, ResultType.INSUFFICIENT_FUNDS, "withdraw");

        int removed = 0;

        TransactionResult result = Transaction.withdrawAllVaults(amount, balance(), vaults);
        removed += result.amount;


        if(ResultType.failureModes.contains(result.type)){
            Inventory inventory =  Util.getInventory(player);
            if(inventory != null){
                result = Transaction.withdraw(inventory, amount - removed);
                removed += result.amount;
            }
        }

        updatePersonalBalance();
        ItemEconomy.getInstance().saveData();
        if((balance() >= amount - removed) && player.isOnline())
            player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Failed to withdraw completely! (Determined cause: Deposit only account).");
        return new TransactionResult(removed, result.type, "withdraw");
    }

    @Override
    public TransactionResult deposit(int amount){
        int numAdded = 0;
        TransactionResult result = Transaction.depositAllVaults(amount, vaults);
        numAdded += result.amount;

        if(ResultType.failureModes.contains(result.type)){
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

    @Override
    public String getAccountType() {
        return "Player Account";
    }

    @Override
    public String toString() {
        return "PlayerAccount{" +
                "player=" + player +
                ", vaults=" + vaults +
                ", taxes=" + taxes +
                ", itemCurrency=" + itemCurrency +
                ", lastPersonalBalance=" + lastPersonalBalance +
                '}';
    }
}
