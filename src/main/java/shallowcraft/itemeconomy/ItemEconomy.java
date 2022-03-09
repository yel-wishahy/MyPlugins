package shallowcraft.itemeconomy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Data.DataSerializer;
import shallowcraft.itemeconomy.Tax.Taxation;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Data.InvalidDataException;
import shallowcraft.itemeconomy.Util.Util;
import lombok.Getter;
import lombok.Setter;

public class ItemEconomy {
    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String name = "ItemEconomy";

    private static ItemEconomy instance;
    @Getter @Setter private Map<String, String> historyStats;
    @Getter @Setter private Map<String, Account> accounts;

    private ItemEconomy(){
        instance = this;
        loadData();
    }

    public static ItemEconomy getInstance(){
        if(instance == null)
            instance = new ItemEconomy();

        return instance;
    }

    public boolean saveData() {
        try {
            File dataFile = DataSerializer.createDataFile(Config.dataFileName);
            DataSerializer.saveAccountsToJSON(accounts, dataFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.info("[ItemEconomy] Failed to save data.");
            return false;
        }
    }

    public boolean loadData() {
        try {
            File dataFile = DataSerializer.getDataFile(Config.dataFileName);
            if (dataFile.exists())
                accounts = DataSerializer.loadDataFromJSON(dataFile);
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


    public boolean hasAccount(OfflinePlayer player) {
        return accounts.containsKey(player.getUniqueId().toString());
    }

    public boolean hasAccount(String id) {
        return accounts.containsKey(id);
    }

    public Account getAccount(OfflinePlayer player) {
        if (hasAccount(player))
            return accounts.get(player.getUniqueId().toString());
        return null;
    }

    public Account getAccount(String name) {
        if (accounts.containsKey(Util.getPlayerID(name)))
            return accounts.get(Util.getPlayerID(name));
        return null;
    }

    public Account getAccount(UUID uuid) {
        if (accounts.containsKey(uuid.toString()))
            return accounts.get(uuid.toString());
        return null;
    }

    public double getBalance(OfflinePlayer player) {
        if (hasAccount(player))
            return Objects.requireNonNull(getAccount(player)).getChequingBalance();
        else
            return 0;
    }

    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }


    public TransactionResult withdrawPlayer(OfflinePlayer player, double amount) {
        ItemEconomy.log.info("[ItemEconomy] WITHDRAWING FROM PLAYER");
        Account holder = accounts.get(player.getUniqueId().toString());

        int toWithdraw = (int) Math.round(amount);
        double taxable = amount - toWithdraw;

        if (holder != null) {
            if (Taxation.tax(taxable))
                try {
                    player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have been taxed " + ChatColor.AQUA + (taxable / amount * 100) + " %!");
                } catch (Exception ignored) {
                }
            return holder.withdraw(toWithdraw);
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "playerNotFound");
        }
    }

    public TransactionResult depositPlayer(OfflinePlayer player, double amount) {
        Account holder = accounts.get(player.getUniqueId().toString());

        int toDeposit = (int) Math.round(amount);
        double taxable = amount - toDeposit;

        if (holder != null) {
            if (Config.enableTaxes && Taxation.tax(taxable))
                try {
                    player.getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have been taxed " + ChatColor.AQUA + (taxable / amount * 100) + " %!");
                } catch (Exception ignored) {
                }
            return holder.deposit(toDeposit);
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "playerNotFound");
        }
    }

    public TransactionResult deposit(String id, double amount) {
        Account holder = accounts.get(id);

        int toDeposit = (int) Math.round(amount);
        double taxable = amount - toDeposit;

        if (holder != null) {
            if (Config.enableTaxes &&  Taxation.tax(taxable))
                try {
                    ((PlayerAccount) holder).getPlayer().getPlayer().sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have been taxed " + ChatColor.AQUA + (taxable / amount * 100) + " %!");
                } catch (Exception ignored) {
                }
            return holder.deposit(toDeposit);
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "playerNotFound");
        }
    }

    public boolean createPlayerAccount(OfflinePlayer player) {
        Player sender = player.getPlayer();
        assert sender != null;
        if (hasAccount(player)) {
            sender.sendMessage("[ItemEconomy] You are already registered for an account!");
        } else {
            Account acc = new PlayerAccount(player);
            accounts.put(acc.getID(), acc);
            sender.sendMessage("[ItemEconomy] You have created a NEW bank account! Lucky spending!");
        }
        return true;
    }
}
