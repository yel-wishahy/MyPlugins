package shallowcraft.itemeconomy;

import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Data.DataManager;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Data.InvalidDataException;
import shallowcraft.itemeconomy.Util.Util;
import lombok.Getter;
import lombok.Setter;

//singleton itemeconomy class that stores various information about the current item economy session
//is called in the Vault economy hook
public class ItemEconomy {
    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String name = "ItemEconomy";

    private static ItemEconomy instance;
    @Getter private boolean isEnabled = false;

    @Getter @Setter private Map<String, String> historyStats;
    @Getter @Setter private Map<String, Account> accounts;
    @Getter @Setter private boolean debugMode;

    private ItemEconomy(){
        instance = this;
        isEnabled = true;
        debugMode = Config.defaultDebug;
    }

    public static ItemEconomy getInstance(){
        if(instance == null)
            instance = new ItemEconomy();

        return instance;
    }

    public boolean saveData() {
        try {
            File dataFile = DataManager.createDataFileJSON(Config.IEdataFileName);
            DataManager.saveDataToJSON(accounts, dataFile);
            return true;
        } catch (IOException e) {
            if(debugMode)
                e.printStackTrace();
            log.info("[ItemEconomy] Failed to save data.");
            return false;
        }
    }

    public boolean loadData() {
        try {
            File dataFile = DataManager.getDataFile(Config.IEdataFileName);
            if (dataFile.exists())
                accounts = DataManager.loadDataFromJSON(dataFile);
            else{
                accounts = new HashMap<>();
            }

            return true;
        } catch (IOException | InvalidDataException e) {
            if(debugMode)
                e.printStackTrace();
            accounts = new HashMap<>();
            log.info("[ItemEconomy] Failed to load data");
            return false;
        }
    }


    public boolean hasAccount(OfflinePlayer player) {
        boolean result = accounts.containsKey(player.getUniqueId().toString()) || accounts.containsKey(player.getName());
        if(debugMode) {
            if (result)
                log.info("[ItemEconomy] in hasAccount, found account with player name: " + player.getName() + "and id " + player.getUniqueId());
            else
                log.info("[ItemEconomy] in hasAccount, COULD NOT FIND account with player name: " + player.getName() + "and id " + player.getUniqueId());
        }
        return result;
    }

    public boolean hasAccount(String id) {
        return accounts.containsKey(id);
    }

    public Account getAccount(OfflinePlayer player) {
        if (accounts.containsKey(player.getUniqueId().toString())) {
            if (debugMode)
                log.info("[ItemEconomy] in getAccount, found account with player name: " + player.getName() + "and id " + player.getUniqueId());
            return accounts.get(player.getUniqueId().toString());
        }

        if (accounts.containsKey(player.getName())){
            if (debugMode)
                log.info("[ItemEconomy] in getAccount, found account with player name: " + player.getName() + "and id " + player.getUniqueId());
        return accounts.get(player.getName());
        }

        if (debugMode)
            log.info("[ItemEconomy] in getAccount, COULD NOT FIND account with player name: " + player.getName() + "and id " + player.getUniqueId());

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
        Account holder = getAccount(player);

        int toWithdraw = (int) amount;
        double buffer = amount - toWithdraw;

        if (holder != null) {
            holder.updateBalanceBuffer(-1*buffer);
            return holder.withdraw(toWithdraw);
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "playerNotFound");
        }
    }

    public TransactionResult depositPlayer(OfflinePlayer player, double amount) {
        Account holder = getAccount(player);

        int toDeposit = (int) amount;
        double buffer = amount - toDeposit;

        if (holder != null) {
            holder.updateBalanceBuffer(buffer);
            return holder.deposit(toDeposit);
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "playerNotFound");
        }
    }

    public TransactionResult deposit(String id, double amount) {
        Account holder = accounts.get(id);

        int toDeposit = (int) amount;
        double buffer = amount - toDeposit;

        if (holder != null) {
            holder.updateBalanceBuffer(buffer);
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

    public void resetHistoryStats(){
        historyStats = new HashMap<>();
        historyStats.put("Circulation", "0");
        historyStats.put("Average Balance", "0");
        historyStats.put("Median Balance", "0");
        historyStats.put("Last Tax Balance", "0");
    }
}
