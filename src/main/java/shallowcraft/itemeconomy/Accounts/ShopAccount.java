package shallowcraft.itemeconomy.Accounts;

import org.bukkit.Material;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.Vault.Vault;

import java.util.ArrayList;
import java.util.List;

public class ShopAccount implements Account{
    private List<Vault> vaults;
    private final Material itemCurrency = Config.currency;
    private final String name = Config.shopID;
    public double balanceBuffer;

    public ShopAccount(){
        vaults = new ArrayList<>();
        balanceBuffer = 0;
    }

    public ShopAccount(double balanceBuffer){
        vaults = new ArrayList<>();
        this.balanceBuffer = balanceBuffer;
    }

    private void convertBalanceBuffer(){
        if(balanceBuffer > 0){
            int toConvert = (int) Math.round(balanceBuffer);
            TransactionResult result = depositCurrency(toConvert);

            balanceBuffer -= result.amount;
        }
    }

    @Override
    public int getBalance() {
        convertBalanceBuffer();
        return Util.getAllVaultsBalance(vaults);
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
    public Material getItemCurrency() {
        return itemCurrency;
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
        return Transaction.withdrawAllVaults(amount, getBalance(), vaults);
    }

    private TransactionResult depositCurrency(int amount){
        return Transaction.depositAllVaults(amount, vaults);
    }

    @Override
    public TransactionResult deposit(int amount) {
        balanceBuffer+=amount;
        return new TransactionResult(amount, ResultType.SUCCESS, "deposit");
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
}
