package shallowcraft.itemeconomy.Accounts;

import org.bukkit.Material;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Vault.Vault;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaxAccount implements Account{
    private List<Vault> vaults;
    private final Material itemCurrency = Config.currency;
    private final String name = Config.taxID;
    public double taxBuffer;

    public TaxAccount(){
        vaults = new ArrayList<>();
        taxBuffer = 0;
    }

    public TaxAccount(double taxBuffer){
        vaults = new ArrayList<>();
        this.taxBuffer = taxBuffer;
    }

    private void convertTaxBuffer(){
        if(taxBuffer > 0){
            int toConvert = (int) Math.round(taxBuffer);
            TransactionResult result = deposit(toConvert);

            taxBuffer -= result.amount;
        }
    }

    @Override
    public int getBalance() {
        convertTaxBuffer();
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

    @Override
    public TransactionResult deposit(int amount) {
        return Transaction.depositAllVaults(amount, vaults);
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
