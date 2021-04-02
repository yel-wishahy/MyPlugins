package shallowcraft.itemeconomy.Core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;
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

    private boolean registerEconomy() {
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
                accounts = new ArrayList<>();

            return true;
        } catch (IOException | InvalidDataException e) {
            e.printStackTrace();
            accounts = new ArrayList<>();
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

        loadData();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            log.info("Only players are supported for this Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getLabel().equals("create_account")) {
            if (Util.hasAccount(player, accounts)) {
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You are already registered for an account!");
            } else {
                accounts.add(new Account(player, Config.currency));
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a NEW bank account! Lucky spending!");
            }
            return true;

        } else if (command.getLabel().equals("balance")) {
            if (Util.hasAccount(player, accounts)) {
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Your balance is: " + ChatColor.AQUA + Objects.requireNonNull(Util.getAccount(player, accounts)).getBalance() + " " + Config.currency.name().toLowerCase());
                return true;
            } else {
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You do not have a bank account");
                return false;
            }
        } else if (command.getLabel().equals("list_accounts")){
            StringBuilder message = new StringBuilder();
            for (Account acc:accounts) {
                message.append(", ").append(acc.getPlayer().getName());
            }

            sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "List of Existing Accounts: " + ChatColor.AQUA + message);
            return true;
        } else if (command.getLabel().equals("create_account_all")) {
            for (OfflinePlayer p:getServer().getOfflinePlayers()) {
                if (Util.hasAccount(p, accounts)) {
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + " is already registered for an account!");
                } else {
                    accounts.add(new Account(p, Config.currency));
                    sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " +  ChatColor.GREEN + "You have created a NEW bank account for " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + "!");
                }
            }
            return true;
        } else if (command.getLabel().equals("reload")) {
            if(loadData())
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " +  ChatColor.GREEN + "Successfully reloaded data");
            else
                sender.sendMessage(ChatColor.GOLD + "[ItemEconomy] " +  ChatColor.RED + "FAILED to load data...files corrupt");

            return true;
        } else if (command.getLabel().equals("baltop")){
            StringBuilder message = new StringBuilder();
            message.append(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "Global Player Balances: \n");
            Map<String, Integer> bals = new HashMap<>();

            for (Account acc:accounts) {
                if(acc != null)
                    bals.put(acc.getPlayer().getName(), acc.getBalance());
            }

            bals = Util.sortByValue(bals);

            List<String> names = new ArrayList<>(bals.keySet());
            int j = 1;
            for (int i = names.size() - 1; i >= 0; i--) {
                String name = names.get(i);
                if(name != null){
                    message.append(j).append(". ").append(ChatColor.GOLD).append(name).append(" ".repeat(20-name.length()));
                    message.append(ChatColor.AQUA).append(bals.get(name)).append(" ").append(Config.currency.name().toLowerCase()).append("\n");
                    j++;
                }
            }

            sender.sendMessage(message.toString());
            return true;
        }

        return false;
    }

    @EventHandler
    public void onCreateVaultSign(SignChangeEvent signEvent) {
        String name = ((TextComponent) Objects.requireNonNull(signEvent.line(1))).content();
        String vaultType = ((TextComponent) Objects.requireNonNull(signEvent.line(2))).content();

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

            if (holder != null && container != null && !Util.isVault(container, accounts)) {
                sign.line(1, Component.text(Objects.requireNonNull(holder.getPlayer().getName())));
                holder.addVault(new ItemVault(container, sign, holder, Config.currency, Util.getVaultType(vaultType)));
                player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.GREEN + "You have created a new Vault!");
            } else {
                if (holder == null)
                    player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot create a vault without an account!");
                if (container == null)
                    player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You cannot create a vault here!");
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
