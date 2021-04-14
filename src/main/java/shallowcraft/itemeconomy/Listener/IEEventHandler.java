package shallowcraft.itemeconomy.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Data.Permissions;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.Vault.ContainerVault;
import shallowcraft.itemeconomy.Vault.Vault;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IEEventHandler implements Listener {

    @EventHandler
    public void onCreateVaultSign(SignChangeEvent signEvent) {
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();
        String id = ((TextComponent) Objects.requireNonNull(signEvent.line(1))).content();
        String vaultType = ((TextComponent) Objects.requireNonNull(signEvent.line(2))).content();

        Player player = signEvent.getPlayer();
        Sign sign = (Sign) signEvent.getBlock().getState();

        if (Util.isValidVaultSignText(signEvent)) {
            Account holder = null;

            if(!id.isEmpty()){
                if(ItemEconomy.getInstance().getServer().getPlayer(id) != null)
                    holder = accounts.get(ItemEconomy.getInstance().getServer().getPlayerUniqueId(id).toString());
            }

            if(holder == null)
                holder = accounts.get(id);

            if(holder == null)
                holder = accounts.get(player.getUniqueId().toString());

            Block container = Util.chestBlock(sign);

            if (holder != null && container != null && !Util.isVault(container)) {
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

    @EventHandler
    public void onBreakVaultSign(BlockBreakEvent blockBreakEvent){
        Block block = blockBreakEvent.getBlock();

        Player player = blockBreakEvent.getPlayer();
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();


        if(block.getState() instanceof Sign && Util.isValidVaultSign((Sign) block.getState())){
            Sign sign = (Sign) block.getState();
            Vault toDestroy = Util.getVaultFromSign(sign);

            if(toDestroy != null && (!player.hasPermission(Permissions.adminPerm) && !player.getName().equals(toDestroy.getHolder().getName()))){
                sign.getBlock().getState().update();
                blockBreakEvent.setCancelled(true);
                player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You are not allowed to destroy this vault");
            } else if (toDestroy != null){
                sign.getBlock().getState().update();
                blockBreakEvent.setCancelled(false);
                toDestroy.destroy();
                player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You have " + ChatColor.BOLD
                        + "DESTROYED" + ChatColor.RESET + " " + ChatColor.YELLOW + toDestroy.getHolder().getName()
                        + "'s" + ChatColor.RED + " vault!");
            } else {
                sign.getBlock().getState().update();
                blockBreakEvent.setCancelled(false);
                player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You have " + ChatColor.BOLD
                        + "DESTROYED" + ChatColor.RESET + " " + ChatColor.RED + "a vault!");
            }
        }
    }

}
