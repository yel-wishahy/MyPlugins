package shallowcraft.itemeconomy.Accounts;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import shallowcraft.itemeconomy.Data.DataUtil;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
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
    private int lastInventoryBalance;
    private int lastStatsBalance;
    private int lastBalance;
    private double balanceBuffer;
    @Getter
    @Setter
    private int netWithdraw;

    public PlayerAccount(Map<String, String> inputData, String ID) {
        OfflinePlayer player = ItemEconomyPlugin.getInstance().getServer().getOfflinePlayer(UUID.fromString(ID));

        int personalBalance = 0;
        int lastSavings = 0;
        int net = 0;
        double buffer = 0.0;
        lastBalance = 0;

        try {
            personalBalance = Integer.parseInt(inputData.get("Personal Balance"));
        } catch (Exception ignored) {
        }
        try {
            lastSavings = Integer.parseInt(inputData.get("Last Savings"));
        } catch (Exception ignored) {
        }
        try {
            net = Integer.parseInt(inputData.get("Net Withdraw"));
        } catch (Exception ignored) {
        }
        try {
            buffer = Double.parseDouble(inputData.get("Balance Buffer"));
        } catch (Exception ignored) {
        }

        this.player = player;
        balanceBuffer = buffer;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        lastInventoryBalance = personalBalance;
        netWithdraw = net;
        this.lastStatsBalance = lastSavings;

        DataUtil.populateAccountVaults(this, inputData, ItemEconomyPlugin.getInstance().getServer());

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

    public PlayerAccount(OfflinePlayer player) {
        this.player = player;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        lastInventoryBalance = 0;
        lastStatsBalance = getBalance(VaultType.REGULAR);
        netWithdraw = 0;
        lastBalance = 0;
    }

    public PlayerAccount(OfflinePlayer player, int personalBalance, int lastProfit) {
        this.player = player;
        vaults = new ArrayList<>();
        taxes = new HashMap<>();
        lastInventoryBalance = personalBalance;
        lastStatsBalance = lastProfit;
        netWithdraw = 0;
        lastBalance = 0;
    }

    public int getLastStatsBalance() {
        return lastStatsBalance;
    }

    @Override
    public int getBalance(VaultType vaultType) {
        if(Bukkit.isPrimaryThread()){
            lastBalance = 0;
            if (updatePersonalBalance())
                lastBalance += lastInventoryBalance;

            if (vaultType == VaultType.DEPOSIT_ONLY)
                lastBalance += Util.getAllVaultsBalance(Util.getVaultsOfType(vaultType, vaults));
            if (vaultType == VaultType.REGULAR || vaultType == VaultType.WITHDRAW_ONLY)
                lastBalance += Util.getAllVaultsBalance(Util.getVaultsOfNotType(VaultType.DEPOSIT_ONLY, vaults));
            else
                lastBalance += Util.getAllVaultsBalance(vaults);
        }

        return lastBalance;
    }


    public int getLastInventoryBalance() {
        return lastInventoryBalance;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public List<Vault> getVaults() {
        return new ArrayList<>(vaults);
    }

    public HashMap<String, Taxable> getTaxes() {
        return new HashMap<>(taxes);
    }

    public void addTax(GeneralTax tax) {
        taxes.put(tax.getTaxName(), tax);
        ItemEconomy.getInstance().saveData();
    }

    public void removeTax(GeneralTax tax) {
        if (taxes.containsValue(tax))
            taxes.remove(tax.getTaxName());
    }

    public void removeTax(String tax) {
        taxes.remove(tax);
    }

    @Override
    public void overrideLoadVaults(List<Vault> override) {
        this.vaults = new ArrayList<>(override);
    }

    public void overrideLoadTaxes(Map<String, GeneralTax> override) {
        this.taxes = new HashMap<>(override);
    }

    public TransactionResult taxAll() {
        int count = 0;
        convertBalanceBuffer();
        for (Taxable tax : taxes.values()) {
            count += tax.tax().amount;
        }

        return new TransactionResult(count, TransactionResult.ResultType.SUCCESS, "tax all");
    }

    private boolean updatePersonalBalance() {
        Inventory inventory = Util.getInventory(player);

        if (inventory != null) {
            lastInventoryBalance = Util.countItem(inventory);
            return true;
        }

        return false;
    }

    public int getProfit() {
        return getBalance(VaultType.ALL) - lastStatsBalance;
    }

    public void updateSavings() {
        lastStatsBalance = getBalance(VaultType.ALL);
    }


    @Override
    public boolean removeVault(Vault vault) {
        return vaults.remove(vault);
    }

    @Override
    public void addVault(Vault vault) {
        vaults.add(vault);
        ItemEconomy.getInstance().saveData();
    }


    @Override
    public TransactionResult withdraw(int amount, VaultType vaultType) {
        TransactionResult result;
        int bal = getBalance(vaultType);

        if (ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("[ItemEconomy] Debug: attempting to withdraw " + amount + " from " + this.getName() + " " + this.getID());

        if (bal < amount) {
            result = new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");
            if (ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("[ItemEconomy] Debug: withdraw result " + amount + " from " + this.getName() + " " + this.getID() + " : " + result);
        }

        int removed = 0;

        if (vaultType == VaultType.DEPOSIT_ONLY)
            result = Transaction.withdrawAllVaults(amount, bal, Util.getVaultsOfType(VaultType.DEPOSIT_ONLY, vaults));
        else if (vaultType == VaultType.REGULAR || vaultType == VaultType.WITHDRAW_ONLY)
            result = Transaction.withdrawAllVaults(amount, bal, Util.getVaultsOfNotType(VaultType.DEPOSIT_ONLY, vaults));
        else
            result = Transaction.withdrawAllVaults(amount, bal, vaults);

        removed += result.amount;


        if (TransactionResult.ResultType.failureModes.contains(result.type)) {
            Inventory inventory = Util.getInventory(player);
            if (inventory != null) {
                result = Transaction.withdraw(inventory, amount - removed);
                removed += result.amount;
            }
        }

        updatePersonalBalance();
        ItemEconomy.getInstance().saveData();

        if ((amount - removed > 0) && player.isOnline())
            player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "Failed to withdraw completely! (Determined cause: Deposit only account).");

        netWithdraw += removed;
        result = new TransactionResult(removed, result.type, "withdraw");

        if (ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("[ItemEconomy] Debug: withdraw result " + amount + " from " + this.getName() + " " + this.getID() + " : " + result);

        return result;
    }

    @Override
    public TransactionResult deposit(int amount) {
        TransactionResult result;
        if (Bukkit.isPrimaryThread()) {
            if (ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("[ItemEconomy] Debug: attempting to deposit " + amount + " into " + this.getName() + " " + this.getID());

            int numAdded = 0;
            result = Transaction.depositAllVaults(amount, vaults);
            numAdded += result.amount;

            if (TransactionResult.ResultType.failureModes.contains(result.type)) {
                Inventory inventory = null;
                try {
                    inventory = player.getPlayer().getInventory();
                } catch (Exception ignore) {
                }

                if (inventory != null) {
                    result = Transaction.deposit(inventory, amount - numAdded);
                    numAdded += result.amount;
                }
            }

            updatePersonalBalance();
            ItemEconomy.getInstance().saveData();
            result = new TransactionResult(numAdded, result.type, "deposit");

            if (ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("[ItemEconomy] Debug: deposit " + amount + " into " + this.getName() + " " + this.getID() + " : " + result);

        } else {

            if (ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("[ItemEconomy] Debug: NOT PRIMARY THREAD. attempting to deposit into buffer due to async execution; " + amount + " into " + this.getName() + " " + this.getID());

            balanceBuffer += amount;

            result = new TransactionResult(amount, TransactionResult.ResultType.SUCCESS, "deposit into buffer");

            if (ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("[ItemEconomy] Debug: buffer deposit " + amount + " into " + this.getName() + " " + this.getID() + " : " + result);
        }

        return result;
    }

    public TransactionResult depositInventory(int amount) {
        TransactionResult result;
        Inventory inventory = null;
        try {
            inventory = player.getPlayer().getInventory();
        } catch (Exception ignore) {
        }

        if (inventory != null) {
            result = Transaction.deposit(inventory, amount);
        } else {
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
    public void updateBalanceBuffer(double amount) {
        balanceBuffer += amount;
    }

    @Override
    public double getBalanceBuffer() {
        return balanceBuffer;
    }

    @Override
    public Map<String, String> getSerializableData() {
        convertBalanceBuffer();
        Map<String, String> outputData = new HashMap<>();

        outputData.put("Personal Balance", String.valueOf(lastInventoryBalance));
        outputData.put("Last Savings", String.valueOf(lastStatsBalance));
        outputData.put("Net Withdraw", String.valueOf(netWithdraw));
        outputData.put("Balance Buffer", String.valueOf(balanceBuffer));

        DataUtil.logVaults(this, outputData);
        DataUtil.logTaxes(this, outputData);

        return outputData;
    }

}
