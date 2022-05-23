package shallowcraft.itemeconomy.Listener;

import com.gamingmesh.jobs.api.JobsPaymentEvent;
import com.gamingmesh.jobs.container.CurrencyType;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Permissions;
import shallowcraft.itemeconomy.Util.Util;
import shallowcraft.itemeconomy.BankVault.ContainerVault;
import shallowcraft.itemeconomy.BankVault.Vault;

import java.util.Map;
import java.util.Objects;

public class IEEventHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent){
        BukkitRunnable task = new BukkitRunnable() {
                /**
                 * When an object implementing interface {@code Runnable} is used
                 * to create a thread, starting the thread causes the object's
                 * {@code run} method to be called in that separately executing
                 * thread.
                 * <p>
                 * The general contract of the method {@code run} is that it may
                 * take any action whatsoever.
                 *
                 * @see Thread#run()
                 */
                @Override
                public void run() {playerJoinEvent.getPlayer().sendMessage(Util.getServerStatsMessage());}
            };
        task.runTaskLater(ItemEconomyPlugin.getInstance(), 100);
    }

    @EventHandler
    public void onCreateVaultSign(SignChangeEvent signEvent) {
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();
        String id = ((TextComponent) Objects.requireNonNull(signEvent.line(1))).content();
        String vaultType = ((TextComponent) Objects.requireNonNull(signEvent.line(2))).content();

        Player player = signEvent.getPlayer();
        Sign sign = (Sign) signEvent.getBlock().getState();

        if (Util.isValidVaultSignText(signEvent)) {
            Account holder = null;

            if (!id.isEmpty()) {
                if (Util.isPlayerName(id))
                    holder = ItemEconomy.getInstance().getAccounts().get(Util.getPlayerID(id));
            }

            if (holder == null)
                holder = accounts.get(id);

            if (holder == null)
                holder = accounts.get(player.getUniqueId().toString());

            Block container = Util.chestBlock(sign);

            if (holder != null && container != null && !Util.isVault(container)) {
                holder.addVault(new ContainerVault(container, sign, holder, Util.getVaultType(vaultType)));
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
    public void onBreakVault(BlockBreakEvent blockBreakEvent) {
        Block block = blockBreakEvent.getBlock();
        Player player = blockBreakEvent.getPlayer();
        Vault toDestroy = null;


        if (block.getState() instanceof Sign && Util.isValidVaultSign((Sign) block.getState())) {
            toDestroy = Util.getVaultFromSign((Sign) block.getState());
        } else if (Config.VaultContainerTypes.contains(block.getType()) && Util.isVault(block)) {
            toDestroy = Util.getVaultFromContainer(block);
        }

        if (toDestroy != null && (!player.hasPermission(Permissions.adminPerm) && !player.getName().equals(toDestroy.getHolder().getName()))) {
            blockBreakEvent.setCancelled(true);
            player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You are not allowed to destroy this vault");
        } else if (toDestroy != null && player.getName().equals(toDestroy.getHolder().getName())) {
            blockBreakEvent.setCancelled(false);
            toDestroy.destroy();
            player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You have " + ChatColor.BOLD
                    + "DESTROYED" + ChatColor.RESET + " " + ChatColor.YELLOW + toDestroy.getHolder().getName()
                    + "'s" + ChatColor.RED + " vault!");
        } else if ((toDestroy != null && (!player.hasPermission(Permissions.adminPerm)))) {
            blockBreakEvent.setCancelled(false);
            player.sendMessage(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You have " + ChatColor.BOLD
                    + "DESTROYED" + ChatColor.RESET + " " + ChatColor.RED + "a vault!");
        }
    }

    @EventHandler
    public void onJobsPayment(JobsPaymentEvent event) {
        try {
            double amount = event.get(CurrencyType.MONEY);
            Account taxAccount = ItemEconomy.getInstance().getAccounts().get(Config.mainTaxDepositID);
            taxAccount.updateBalanceBuffer(-1 * amount);
            taxAccount.convertBalanceBuffer();
            if (ItemEconomy.getInstance().isDebugMode()) {
                ItemEconomy.log.info("[ItemEconomy] JobsReborn API Listener detected job payment event");
                ItemEconomy.log.info("[ItemEconomy] withdrew " + amount + " from " + taxAccount.getID() + " due to jobs payment.");
            }
        } catch (Exception e){
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
        }

    }

}
