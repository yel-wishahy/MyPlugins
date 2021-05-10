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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralAccount implements Account {
    private List<Vault> vaults;
    private final Material itemCurrency = Config.currency;
    private final String name;
    public double balanceBuffer;
    public boolean isTaxDeposit;

    public GeneralAccount(Map<String, String> inputData, String ID){
        double buffer = 0;

        try{buffer = Double.parseDouble(inputData.get("Buffer")); }
        catch (Exception ignored){}

        vaults = new ArrayList<>();
        this.name = ID;
        balanceBuffer = 0;
        setTaxDeposit();

        DataUtil.populateAccount(this, inputData, ItemEconomyPlugin.getInstance().getServer());
    }

    public GeneralAccount(String name){
        vaults = new ArrayList<>();
        this.name = name;
        balanceBuffer = 0;
        setTaxDeposit();
    }

    public GeneralAccount(double balanceBuffer, String name){
        vaults = new ArrayList<>();
        this.name = name;
        this.balanceBuffer = balanceBuffer;
        setTaxDeposit();
    }

    private void setTaxDeposit(){
        if(name.toLowerCase().contains("tax"))
            isTaxDeposit = true;
        else
            isTaxDeposit = false;
    }

    private void convertBalanceBuffer(){
        if(balanceBuffer > 0){
            int toConvert = (int) Math.round(balanceBuffer);
            TransactionResult result = depositCurrency(toConvert);

            balanceBuffer -= result.amount;
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

    private TransactionResult depositCurrency(int amount){
        return Transaction.depositAllVaults(amount, vaults);
    }

    @Override
    public TransactionResult deposit(int amount) {
        balanceBuffer+=amount;
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
        DataUtil.logVaults(this, outputData);

        return outputData;
    }
}
