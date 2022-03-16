package shallowcraft.itemeconomy.Accounts;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import shallowcraft.itemeconomy.Data.DataUtil;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.SmartShop.SmartShop;
import shallowcraft.itemeconomy.SmartShop.SmartShopConfig;
import shallowcraft.itemeconomy.Tax.taxable.GeneralTax;
import shallowcraft.itemeconomy.Tax.taxable.Taxable;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.BankVault.VaultType;
import java.util.*;

public class PlayerAccount implements Account {
    private final OfflinePlayer player;
    private List<Vault> vaults;
    private Map<String, Taxable> taxes;
    private int lastPersonalBalance;
    private int lastBalance;
    @Getter @Setter private int netWithdraw;

    public PlayerAccount(Map<String, String> inputData, String ID){
        OfflinePlayer player = ItemEconomyPlugin.getInstance().getServer().getOfflinePlayer(UUID.fromString(ID));

        int personalBalance = 0;
        int lastSavings = 0;
        int net = 0;

        try{ personalBalance = Integer.parseInt(inputData.get("Personal Balance"));}
        catch (Exception ignored){}
        try { lastSavings = Integer.parseInt(inputData.get("Last Savings"));}
        catch (Exception ignored){}
        try { net = Integer.parseInt(inputData.get("Net Withdraw"));}
        catch (Exception ignored){}

        this.player = player;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        lastPersonalBalance = personalBalance;
        netWithdraw = net;
        this.lastBalance = lastSavings;

        DataUtil.populateAccountVaults(this, inputData, ItemEconomyPlugin.getInstance().getServer());
        DataUtil.populateAccountTaxes(this, inputData);

        //load taxes later, as this requires other accounts to be loaded as well for tax deposit
        Account thisAccount = this;
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                DataUtil.populateAccountTaxes(thisAccount, inputData);
            }
        };
        task.runTaskLater(ItemEconomyPlugin.getInstance(), 100);
    }

    public PlayerAccount(OfflinePlayer player){
        this.player = player;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        lastPersonalBalance = 0;
        lastBalance = getChequingBalance();
        netWithdraw = 0;
    }

    public PlayerAccount(OfflinePlayer player, int personalBalance, int lastProfit){
        this.player = player;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        lastPersonalBalance = personalBalance;
        lastBalance = lastProfit;
        netWithdraw = 0;
    }

    public int getLastBalance(){
        return lastBalance;
    }

    @Override
    public int getChequingBalance() {
        int count = 0;

        if(updatePersonalBalance())
            count+=lastPersonalBalance;

        return count + Util.getAllVaultsBalance(Util.getVaultsOfNotType(VaultType.DEPOSIT_ONLY, vaults));
    }

    @Override
    public int getBalance() {
        return getForcedBalance();
    }

    @Override
    public int getBalance(VaultType vaultType) {
        int count = 0;

        if(updatePersonalBalance())
            count+=lastPersonalBalance;

        return count + Util.getAllVaultsBalance(Util.getVaultsOfType(vaultType, vaults));
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

    public void addTax(GeneralTax tax){
        taxes.put(tax.getTaxName(), tax);
        ItemEconomy.getInstance().saveData();
    }

    public void removeTax(GeneralTax tax){
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

    public void overrideLoadTaxes(Map<String, GeneralTax> override){
        this.taxes = new HashMap<>(override);
    }

    public TransactionResult taxAll(){
        int count = 0;
        for (Taxable tax: taxes.values()) {
            count += tax.tax().amount;
        }

        return new TransactionResult(count, TransactionResult.ResultType.SUCCESS, "tax all");
    }

    private boolean updatePersonalBalance(){
        Inventory inventory = Util.getInventory(player);

        if(inventory != null){
            lastPersonalBalance = Util.countItem(inventory);
            return true;
        }

        return false;
    }

    private int getForcedBalance(){
        int count = 0;

        if(updatePersonalBalance())
            count+=lastPersonalBalance;

        return count + Util.getAllVaultsBalance(vaults);
    }

    public int getProfit(){
        return getBalance() - lastBalance;
    }

    public void updateSavings(){
        lastBalance = getBalance();
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
        if(getChequingBalance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        int removed = 0;

        TransactionResult result = Transaction.withdrawAllVaults(amount, getChequingBalance(), vaults);
        removed += result.amount;


        if(TransactionResult.ResultType.failureModes.contains(result.type)){
            Inventory inventory =  Util.getInventory(player);
            if(inventory != null){
                result = Transaction.withdraw(inventory, amount - removed);
                removed += result.amount;
            }
        }

        updatePersonalBalance();
        ItemEconomy.getInstance().saveData();
        if((amount - removed > 0) && player.isOnline())
            player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Failed to withdraw completely! (Determined cause: Deposit only account).");

        netWithdraw+=removed;
        return new TransactionResult(removed, result.type, "withdraw");
    }

    @Override
    public TransactionResult forcedWithdraw(int amount){
        if(getBalance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        int removed = 0;

        TransactionResult result = Transaction.forceWithdrawAllVaults(amount, getBalance(), vaults);
        removed += result.amount;


        if(TransactionResult.ResultType.failureModes.contains(result.type)){
            Inventory inventory =  Util.getInventory(player);
            if(inventory != null){
                result = Transaction.withdraw(inventory, amount - removed);
                removed += result.amount;
            }
        }

        updatePersonalBalance();
        ItemEconomy.getInstance().saveData();
        if((amount - removed > 0) && player.isOnline())
            player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Failed to withdraw completely! (Determined cause: Deposit only account).");
        return new TransactionResult(removed, result.type, "withdraw");
    }

    @Override
    public TransactionResult deposit(int amount){
        int numAdded = 0;
        TransactionResult result = Transaction.depositAllVaults(amount, vaults);
        numAdded += result.amount;

        if(TransactionResult.ResultType.failureModes.contains(result.type)){
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

    public TransactionResult depositInventory(int amount){
        TransactionResult result;
        Inventory inventory =  null;
        try{
            inventory = player.getPlayer().getInventory();
        } catch (Exception ignore){
        }

        if(inventory != null){
            result = Transaction.deposit(inventory, amount);
        } else{
            result = new TransactionResult(0, TransactionResult.ResultType.FAILURE, "fail");
        }

        return result;
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
    public TransactionResult transfer(VaultType source, VaultType destination, int amount) {
        List<Vault> sources = Util.getVaultsOfType(source, vaults);
        List<Vault> destinations = Util.getVaultsOfType(destination, vaults);
        return Transaction.transferVaults(sources, destinations, amount);
    }

    @Override
    public Map<String, String> getSerializableData() {
        Map<String, String> outputData = new HashMap<>();

        outputData.put("Personal Balance", String.valueOf(lastPersonalBalance));
        outputData.put("Last Savings", String.valueOf(lastBalance));
        outputData.put("Net Withdraw", String.valueOf(netWithdraw));

        DataUtil.logVaults(this, outputData);
        DataUtil.logTaxes(this, outputData);

        return outputData;
    }

}
