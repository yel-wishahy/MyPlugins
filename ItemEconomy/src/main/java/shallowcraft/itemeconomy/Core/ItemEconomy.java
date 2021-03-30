package shallowcraft.itemeconomy.Core;

import net.kyori.adventure.text.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import net.kyori.*;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Util.DataLoader;
import shallowcraft.itemeconomy.Util.InvalidDataException;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.VaultHook.Economy_ItemEconomy;

public class ItemEconomy extends JavaPlugin implements Listener {
    public static final Logger log = Logger.getLogger("Minecraft");
    private List<Account> accounts;
    private static ItemEconomy instance;
    public final static String name = "ItemEconomy";

    public static ItemEconomy getInstance() {
        return instance;
    }

    private boolean setupEconomy() {
        if(getServer().getPluginManager().isPluginEnabled("Vault")) {
            getServer().getServicesManager().register(Economy.class, new Economy_ItemEconomy(), this, ServicePriority.Normal);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDisable() {
        saveData();
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    public void saveData() {
        try {
            File dataFile = DataLoader.createDataFile(Config.dataFileName);
            DataLoader.saveDataToJSON(accounts, dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("[ItemEconomy] Failed to save data.");
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            File dataFile = DataLoader.getDataFile(Config.dataFileName);
            if (dataFile.exists())
                accounts = DataLoader.loadJSON(dataFile, Bukkit.getServer());
            else
                accounts = new ArrayList<>();
        } catch (IOException | InvalidDataException e) {
            e.printStackTrace();
            accounts = new ArrayList<>();
            log.info("[ItemEconomy] Failed to load data");
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getLabel().equals("create_account")) {
            if (Util.hasAccount(player, accounts)) {
                sender.sendMessage("[ItemEconomy] You are already registered for an account!");
            } else {
                accounts.add(new Account(player, Config.currency));
                sender.sendMessage("[ItemEconomy] You have created a NEW bank account! Lucky spending!");
            }
            return true;

        } else if (command.getLabel().equals("balance") && args.length < 1) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if (Util.hasAccount(player, accounts)) {
                sender.sendMessage("[ItemEconomy] Your balance is: " + Objects.requireNonNull(Util.getAccount(player, accounts)).getBalance() + " Diamonds");
            } else {
                sender.sendMessage("[ItemEconomy] You do not have a bank account");
            }
            return true;
        } else if (command.getLabel().equals("list_accounts")){
            StringBuilder message = new StringBuilder();
            for (Account acc:accounts) {
                message.append(", ").append(acc.getPlayer().getName());
            }

            sender.sendMessage("[ItemEconomy] List of Existing Accounts: " + message);
            return true;
        } else if (command.getLabel().equals("balance") && args.length >= 1) {
            OfflinePlayer holder = getServer().getOfflinePlayer(Objects.requireNonNull(getServer().getPlayerUniqueId(args[0])));
            sender.sendMessage("[ItemEconomy] " + holder.getName() + "'s balance is: " + Objects.requireNonNull(Util.getAccount(holder, accounts)).getBalance() + " Diamonds");
            return true;
        }

        return false;
    }

    @EventHandler
    public void onCreateVaultSign(SignChangeEvent signEvent) {
        String name = ((TextComponent) Objects.requireNonNull(signEvent.line(1))).content();

        Player player = signEvent.getPlayer();
        Sign sign = (Sign) signEvent.getBlock().getState();

        if (Util.isValidVaultSign(signEvent)) {
            Account holder = null;

            if(!name.isEmpty()){
                holder = Util.getAccount(getServer().getOfflinePlayer(Objects.requireNonNull(getServer().getPlayerUniqueId(name))), accounts);
            } else {
                holder = Util.getAccount(player, accounts);
            }

            Block container = Util.chestBlock(sign);

            if (holder != null && container != null) {
                holder.addVault(new ItemVault(container, sign, holder, Config.currency));
                player.sendMessage("[ItemEconomy] Created new vault!");
            } else {
                if (holder == null)
                    player.sendMessage("[ItemEconomy] You cannot create a vault without an account");
                if (container == null)
                    player.sendMessage("[ItemEconomy] You cannot create a vault without a proper vault container!");
            }
        }
    }


    public boolean hasAccount(OfflinePlayer player) {
        return Util.hasAccount(player, accounts);
    }

    public double getBalance(OfflinePlayer player) {
        if(hasAccount(player))
            return Objects.requireNonNull(Util.getAccount(player, accounts)).getBalance();
        else
            return 0;
    }

    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    public TransactionResult withdrawPlayer(OfflinePlayer player, int amount){
        Account holder = Util.getAccount(player, accounts);

        if(holder !=null){
            return holder.withdraw(amount);
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "playerNotFound");
        }
    }

    public TransactionResult depositPlayer(OfflinePlayer player, int amount){
        Account holder = Util.getAccount(player, accounts);

        if(holder !=null){
            return holder.deposit(amount);
        } else {
            return new TransactionResult(0, TransactionResult.ResultType.FAILURE, "playerNotFound");
        }

    }

    public boolean createPlayerAccount(OfflinePlayer player){
        Player sender = player.getPlayer();
        assert sender != null;
        if (Util.hasAccount(player, accounts)) {
            sender.sendMessage("[ItemEconomy] You are already registered for an account!");
        } else {
            accounts.add(new Account(player, Config.currency));
            sender.sendMessage("[ItemEconomy] You have created a NEW bank account! Lucky spending!");
        }
        return true;
    }




}
