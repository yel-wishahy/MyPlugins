package shallowcraft.itemeconomy;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Accounts.TaxAccount;
import shallowcraft.itemeconomy.Commands.Commands;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Transaction.TransactionResult;
import shallowcraft.itemeconomy.Vault.ContainerVault;
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

        Bukkit.getPluginManager().registerEvents(this, this);
        loadData();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, String[] args) {
        return Commands.onCommand(sender,command,commandLabel,args,accounts);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Commands.onTabComplete(sender, command, alias, args);
    }

    @EventHandler
    public void onCreateVaultSign(SignChangeEvent signEvent) {
        String id = ((TextComponent) Objects.requireNonNull(signEvent.line(1))).content();
        String vaultType = ((TextComponent) Objects.requireNonNull(signEvent.line(2))).content();

        Player player = signEvent.getPlayer();
        Sign sign = (Sign) signEvent.getBlock().getState();

        if (Util.isValidVaultSign(signEvent)) {
            Account holder = null;

            if(!id.isEmpty()){
                if(getServer().getPlayer(id) != null)
                    holder = Util.getAccount(getServer().getPlayerUniqueId(id).toString(), accounts);
                else
                    holder = Util.getAccount(id, accounts);
            }

            if(holder == null)
                holder = Util.getAccount(player.getUniqueId().toString(), accounts);

            Block container = Util.chestBlock(sign);

            if (holder != null && container != null && !Util.isVault(container, accounts)) {
                sign.line(1, Component.text(Objects.requireNonNull(holder.getName())));
                holder.addVault(new ContainerVault(container, sign, holder, Config.currency, Util.getVaultType(vaultType)));
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
        return Util.hasAccount(player.getUniqueId().toString(), accounts);
    }

    public double getBalance(OfflinePlayer player) {
        if(hasAccount(player))
            return Objects.requireNonNull(Util.getAccount(player.getUniqueId().toString(), accounts)).getBalance();
        else
            return 0;
    }

    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    public TransactionResult withdrawPlayer(OfflinePlayer player, double amount){
        Account holder = Util.getAccount(player.getUniqueId().toString(), accounts);

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
        Account holder = Util.getAccount(player.getUniqueId().toString(), accounts);
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
        if (Util.hasAccount(player.getUniqueId().toString(), accounts)) {
            sender.sendMessage("[ItemEconomy] You are already registered for an account!");
        } else {
            accounts.add(new PlayerAccount(player, Config.currency));
            sender.sendMessage("[ItemEconomy] You have created a NEW bank account! Lucky spending!");
        }
        return true;
    }

    private boolean tax(double amount){
        for (Account acc: accounts){
            if(acc instanceof TaxAccount){
                ((TaxAccount) acc).taxBuffer+=amount;
                return true;
            }
        }
        return false;
    }




}
