package shallowcraft.itemeconomy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Accounts.TaxAccount;
import shallowcraft.itemeconomy.Commands.IECommand;
import shallowcraft.itemeconomy.Commands.IETabCompleter;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.Listener.IEEventHandler;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Util.DataLoader;
import shallowcraft.itemeconomy.Util.InvalidDataException;
import shallowcraft.itemeconomy.VaultEconomyHook.Economy_ItemEconomy;

public class ItemEconomy extends JavaPlugin{
    public static final Logger log = Logger.getLogger("Minecraft");
    private Map<String, Account> accounts;
    private static ItemEconomy instance;
    public final static String name = "ItemEconomy";

    public static ItemEconomy getInstance() {
        return instance;
    }

    public HashMap<String, Account> getAccounts(){return (HashMap<String, Account>) accounts;}

    private boolean registerEconomy() {
        if(getServer().getPluginManager().isPluginEnabled("Vault")) {
            getServer().getServicesManager().register(Economy.class, new Economy_ItemEconomy(), this, ServicePriority.Normal);
            return true;
        } else {
            return false;
        }
    }

    private void registerEventHandler(){
        getServer().getPluginManager().registerEvents(new IEEventHandler(), this);
    }

    private void registerCommands(){
        try{
            this.getCommand(Config.IECommand).setExecutor(new IECommand());
        } catch (Exception ignored){
            log.info("[ItemEconomy] Failed register Command!");
        }
    }

    private void registerCommandHelper(){
        try{
            getServer().getPluginCommand(Config.IECommand).setTabCompleter(new IETabCompleter());
        } catch (Exception ignored){
            log.info("[ItemEconomy] Failed register tab completer command helper!");
        }
    }

    @Override
    public void onDisable() {
        saveData();
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    public boolean saveData() {
        try {
            File dataFile = DataLoader.createDataFile(Config.dataFileName);
            DataLoader.saveDataToJSON(accounts, dataFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.info("[ItemEconomy] Failed to save data.");
            return false;
        }
    }

    public boolean loadData(){
        try {
            File dataFile = DataLoader.getDataFile(Config.dataFileName);
            if (dataFile.exists())
                accounts = DataLoader.loadJSON(dataFile, Bukkit.getServer());
            else
                accounts = new HashMap<>();

            return true;
        } catch (IOException | InvalidDataException e) {
            e.printStackTrace();
            accounts = new HashMap<>();
            log.info("[ItemEconomy] Failed to load data");
            return false;
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!registerEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerEventHandler();
        registerCommands();
        registerCommandHelper();
        loadData();
    }


    public boolean hasAccount(OfflinePlayer player) {
        return accounts.containsKey(player.getUniqueId().toString());
    }

    public boolean hasAccount(String id) {
        return accounts.containsKey(id);
    }

    public Account getAccount(OfflinePlayer player){
        if(hasAccount(player))
            return accounts.get(player.getUniqueId().toString());
        return null;
    }

    public double getBalance(OfflinePlayer player) {
        if(hasAccount(player))
            return Objects.requireNonNull(getAccount(player)).getBalance();
        else
            return 0;
    }

    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }


    public TransactionResult withdrawPlayer(OfflinePlayer player, double amount){
        Account holder = accounts.get(player.getUniqueId().toString());

        int toWithdraw = (int) Math.round(amount);
        double taxable = amount - toWithdraw;

        if(holder !=null){
            if(tax(taxable))
                try{
                    player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have been taxed " + ChatColor.AQUA + (taxable/amount * 100) + " %!");
                } catch (Exception ignored){
                }
            return holder.withdraw(toWithdraw);
        } else {
            return new TransactionResult(0, ResultType.FAILURE, "playerNotFound");
        }
    }

    public TransactionResult depositPlayer(OfflinePlayer player, double amount){
        Account holder = accounts.get(player.getUniqueId().toString());

        int toDeposit = (int) Math.round(amount);
        double taxable = amount - toDeposit;

        if(holder !=null){
            if(tax(taxable))
                try{
                    player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have been taxed " + ChatColor.AQUA + (taxable/amount * 100) + " %!");
                } catch (Exception ignored){
                }
            return holder.deposit(toDeposit);
        } else {
            return new TransactionResult(0, ResultType.FAILURE, "playerNotFound");
        }
    }

    public boolean createPlayerAccount(OfflinePlayer player){
        Player sender = player.getPlayer();
        assert sender != null;
        if (hasAccount(player)) {
            sender.sendMessage("[ItemEconomy] You are already registered for an account!");
        } else {
            Account acc = new PlayerAccount(player, Config.currency);
            accounts.put(acc.getID(), acc);
            sender.sendMessage("[ItemEconomy] You have created a NEW bank account! Lucky spending!");
        }
        return true;
    }

    private boolean tax(double amount){
        if(hasAccount(Config.taxID)){
            TaxAccount acc = (TaxAccount) accounts.get(Config.taxID);
            acc.taxBuffer+=amount;
            return true;
        }

        return false;
    }




}
