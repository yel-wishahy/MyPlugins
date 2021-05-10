package shallowcraft.itemeconomy.SmartShop.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.SmartShop.SmartShopUtil;

public class SSEventHandler implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent){
        String id = playerJoinEvent.getPlayer().getUniqueId().toString();

        if(SmartShopUtil.hasPendingOrders(id)){
            Runnable task = () -> playerJoinEvent.getPlayer().sendMessage(SmartShopUtil.getJoinMessage(id));
            ItemEconomyPlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(ItemEconomyPlugin.getInstance(), task, 200);
        }
    }
}
