package shallowcraft.itemeconomy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemEconomy extends JavaPlugin implements Listener {
    public static final Logger log = Logger.getLogger("Minecraft");
    private static List<Account> accounts;

    @Override
    public void onDisable() {
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
        try {
            File dataFile = DataLoader.getDataFile(Config.dataFileName);
            if(dataFile.exists())
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
        if(!(sender instanceof Player)) {
            log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if(command.getLabel().equals("create_account")) {
            if(Util.hasAccount(player, accounts)){
                sender.sendMessage("[ItemEconomy] You are already registered for an account!");
            } else {
                accounts.add(new Account(player, Config.currency));
                sender.sendMessage("[ItemEconomy] You have created a NEW bank account! Lucky spending!");
            }
            return true;

        } else if(command.getLabel().equals("balance")) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if(Util.hasAccount(player, accounts)) {
                sender.sendMessage("[ItemEconomy] Your balance is: " + Objects.requireNonNull(Util.getAccount(player, accounts)).getBalance() + " Diamonds");
            } else {
                sender.sendMessage("[ItemEconomy] You do not have a bank account");
            }
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void onCreateVaultSign(SignChangeEvent signEvent) {
        Player player = signEvent.getPlayer();
        Sign sign = (Sign) signEvent.getBlock().getState();
        if (Objects.requireNonNull(signEvent.line(0)).toString().contains("[Vault]")){
            Account holder = Util.getAccount(player, accounts);
            Block container = Util.chestBlock(sign);

            if (holder != null && container != null){
                holder.addVault(new ItemVault(container, sign, holder, Config.currency));
                player.sendMessage("[ItemEconomy] Created new vault!");
                player.sendMessage("[ItemEconomy] " + container.getType().toString());
            } else {
                if(holder == null)
                    player.sendMessage("[ItemEconomy] You cannot create a vault without an account");
                if(container == null)
                    player.sendMessage("[ItemEconomy] You cannot create a vault without a vault container!");
            }
        }
    }
}
