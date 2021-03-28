package shallowcraft.itemeconomy;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
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

public class ItemEconomy extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    private static List<Account> accounts;
    public final static Material currency = Material.DIAMOND;

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        accounts = new ArrayList<Account>();
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if(command.getLabel().equals("createEcoAccount")) {
            if(Util.hasAccount(player, accounts)){
                sender.sendMessage("[ItemEconomy] You are already registered for an account!");
            } else {
                accounts.add(new Account(player));
                sender.sendMessage("[ItemEconomy] You have created a NEW bank account! Lucky spending!");
            }
            return true;

        } else if(command.getLabel().equals("balance")) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if(Util.hasAccount(player, accounts)) {
                sender.sendMessage("[ItemEconomy] Your balance is: " + Util.getAccount(player, accounts).getBalance() + " Diamonds");
            } else {
                sender.sendMessage("[ItemEconomy] You do not have a bank account");
            }
            return true;
        } else {
            return false;
        }
    }
}
