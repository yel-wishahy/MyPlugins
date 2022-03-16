package shallowcraft.itemeconomy.Accounts;

import org.bukkit.Material;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Data.DataUtil;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;

import java.util.*;

public class GeneralAccount implements Account {
    private List<Vault> vaults;
    private final Material itemCurrency = Config.currency;
    private final String name;
    public double balanceBuffer;
    public boolean isMainTaxDeposit;

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
    }

    public GeneralAccount(String name){
        vaults = new ArrayList<>();
        this.name = name;
        balanceBuffer = 0;
        isMainTaxDeposit = name.toLowerCase().contains("tax");
    }

    public GeneralAccount(double balanceBuffer, String name){
        vaults = new ArrayList<>();
        this.name = name;
        this.balanceBuffer = balanceBuffer;
        isMainTaxDeposit = name.toLowerCase().contains("tax");
    }

    private TransactionResult convertBalanceBuffer(){
        if(balanceBuffer >= 1) {
            TransactionResult result = Transaction.depositAllVaults((int)balanceBuffer, vaults);
            balanceBuffer -= result.amount;
            return result;
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "balance buffer too small");
        }
    }

    @Override
    public int getChequingBalance() {
        convertBalanceBuffer();
        return Util.getAllVaultsBalance(Util.getVaultsOfNotType(VaultType.DEPOSIT_ONLY, vaults));
    }

    @Override
    public int getBalance() {
        convertBalanceBuffer();
        return Util.getAllVaultsBalance(vaults);
    }

    @Override
    public int getBalance(VaultType vaultType) {
        convertBalanceBuffer();
        return Util.getAllVaultsBalance(Util.getVaultsOfType(vaultType, vaults));
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
    public TransactionResult withdraw(int amount) {
        if(getChequingBalance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");
        return Transaction.withdrawAllVaults(amount, getChequingBalance(), vaults);
    }

    @Override
    public TransactionResult forcedWithdraw(int amount){
        if(getBalance() < amount)
            return new TransactionResult(0, TransactionResult.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        return Transaction.forceWithdrawAllVaults(amount, getBalance(), vaults);
    }

    @Override
    public TransactionResult deposit(int amount) {
        balanceBuffer+=amount;
        convertBalanceBuffer();
        return new TransactionResult(amount, TransactionResult.ResultType.SUCCESS, "deposit");
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
    public TransactionResult transfer(VaultType source, VaultType destination, int amount) {
        List<Vault> sources = Util.getVaultsOfType(source, vaults);
        List<Vault> destinations = Util.getVaultsOfType(destination, vaults);
        return Transaction.transferVaults(sources, destinations, amount);
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
