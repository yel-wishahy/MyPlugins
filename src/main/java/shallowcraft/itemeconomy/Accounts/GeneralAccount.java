package shallowcraft.itemeconomy.Accounts;

import org.bukkit.Bukkit;
import shallowcraft.itemeconomy.Data.DataUtil;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Transaction.TransactionUtils;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;

import java.util.*;

public class GeneralAccount implements Account {
    private List<Vault> vaults;
    private final String name;
    public boolean isMainTaxDeposit;
    private double balanceBuffer;
    private int lastBalance;

    public GeneralAccount(Map<String, String> inputData, String ID){
        this.name = ID;
        try{
            this.balanceBuffer  = Double.parseDouble(inputData.get("Buffer"));
        } catch (Exception ignored){
            balanceBuffer = 0;
        }

        try{
            this.isMainTaxDeposit = Boolean.parseBoolean(inputData.get("isMainTaxDeposit"));
        } catch (Exception ignored){
            isMainTaxDeposit = name.toLowerCase().contains("tax");
        }

        vaults = new ArrayList<>();

        DataUtil.populateAccountVaults(this, inputData, ItemEconomyPlugin.getInstance().getServer());
        lastBalance = 0;
    }

    public GeneralAccount(String name){
        vaults = new ArrayList<>();
        this.name = name;
        balanceBuffer = 0;
        isMainTaxDeposit = name.toLowerCase().contains("tax");
        lastBalance = 0;
    }

    public GeneralAccount(double balanceBuffer, String name){
        vaults = new ArrayList<>();
        this.name = name;
        this.balanceBuffer = balanceBuffer;
        isMainTaxDeposit = name.toLowerCase().contains("tax");
        lastBalance = 0;
    }

    @Override
    public int getBalance(VaultType vaultType) {
        if(Bukkit.isPrimaryThread()) {

            if (vaultType == VaultType.DEPOSIT_ONLY)
                lastBalance= Util.getAllVaultsBalance(Util.getVaultsOfType(vaultType, vaults));
            if (vaultType == VaultType.REGULAR || vaultType == VaultType.WITHDRAW_ONLY)
                lastBalance= Util.getAllVaultsBalance(Util.getVaultsOfNotType(VaultType.DEPOSIT_ONLY, vaults));
            else
                lastBalance= Util.getAllVaultsBalance(vaults);
        }

        return lastBalance;
    }

    @Override
    public List<Vault> getVaults() {
        return new ArrayList<>(vaults);
    }

    @Override
    public void overrideLoadVaults(List<Vault> override) {
        vaults = new ArrayList<>(override);
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
    public Transaction withdraw(int amount, VaultType vaultType) {
        convertBalanceBuffer();
        Transaction result;
        int bal = getBalance(vaultType);

        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("[ItemEconomy] Debug: attempting to withdraw " + amount + " from " + this.name + " " + this.getID());

        if(bal < amount)
            return new Transaction(0, Transaction.ResultType.INSUFFICIENT_FUNDS, "withdraw",this, Transaction.TransactionType.WITHDRAW);

        if (vaultType == VaultType.DEPOSIT_ONLY)
            result = TransactionUtils.withdrawAllVaults(amount, bal, Util.getVaultsOfType(VaultType.DEPOSIT_ONLY, vaults));
        else if (vaultType == VaultType.REGULAR || vaultType == VaultType.WITHDRAW_ONLY)
            result = TransactionUtils.withdrawAllVaults(amount, bal, Util.getVaultsOfNotType(VaultType.DEPOSIT_ONLY, vaults));
        else
            result = TransactionUtils.withdrawAllVaults(amount, bal, vaults);

        ItemEconomy.getInstance().saveData();

        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("[ItemEconomy] Debug: withdraw result " + amount + " from " + this.name + " " + this.getID() + " : " + result);

        return result;
    }

    @Override
    public Transaction deposit(int amount) {
        Transaction result;
        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("[ItemEconomy] Debug: attempting to deposit " + amount + " into " + this.name + " " + this.getID());
        balanceBuffer+=amount;

        result = new Transaction(amount, Transaction.ResultType.SUCCESS, "deposit",this, Transaction.TransactionType.DEPOSIT);

        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("[ItemEconomy] Debug: deposit " + amount + " into " + this.name + " " + this.getID() + " : " + result);

        return  result;
    }

    @Override
    public String getID() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String getAccountType() {
        return "General Account";
    }

    @Override
    public Transaction transfer(VaultType source, VaultType destination, int amount) {
        List<Vault> sources = Util.getVaultsOfType(source, vaults);
        List<Vault> destinations = Util.getVaultsOfType(destination, vaults);
        return TransactionUtils.transferVaults(sources, destinations, amount);
    }

    @Override
    public void transactionBalanceBuffer(double amount) {
        balanceBuffer += amount;
    }

    @Override
    public double getBalanceBuffer() {
        return balanceBuffer;
    }

    @Override
    public Map<String, String> getSerializableData() {
        Map<String, String> outputData = new HashMap<>();

        outputData.put("Buffer", String.valueOf(balanceBuffer));
        outputData.put("isMainTaxDeposit", String.valueOf(isMainTaxDeposit));
        DataUtil.logVaults(this, outputData);

        return outputData;
    }
}
