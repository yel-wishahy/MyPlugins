package shallowcraft.itemeconomy.ThirdPartyIntegration.smartshop.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.ThirdPartyIntegration.smartshop.SmartShopUtil;

import org.maxgamer.quickshop.api.event.ShopDeleteEvent;
import org.maxgamer.quickshop.api.event.ShopTaxEvent;


public class SSEventHandler implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent){
        String id = playerJoinEvent.getPlayer().getUniqueId().toString();

        if(SmartShopUtil.hasPendingOrders(id)){
            Runnable task = () -> playerJoinEvent.getPlayer().sendMessage(SmartShopUtil.getJoinMessage(id));
            ItemEconomyPlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(ItemEconomyPlugin.getInstance(), task, 200);
        }
    }

    @EventHandler
    public void onShopDelete(ShopDeleteEvent shopDeleteEvent){
        try {
            if(ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("[ItemEconomy: SmartShop] Attempting to withdraw money from tax account due to quickshop shop delete event.");

            Account taxAccount = ItemEconomy.getInstance().getAccounts().get((String)Config.TaxesConfig.get("mainTaxDepositID"));
            taxAccount.updateBalanceBuffer(-1*(int)Config.SmartShopConfig.get("shopDepositCost"));
            taxAccount.convertBalanceBuffer();
        } catch (Exception e){
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
        }
    }

    @EventHandler
    public void onPurchaseTax(ShopTaxEvent shopTaxEvent){
        try {
            if(ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("[ItemEconomy: SmartShop] Attempting to deposit into tax account due to quickshop tax event.");
            Account taxAccount = ItemEconomy.getInstance().getAccounts().get((String)Config.TaxesConfig.get("mainTaxDepositID"));
            taxAccount.updateBalanceBuffer(shopTaxEvent.getTax());
            taxAccount.convertBalanceBuffer();
        } catch (Exception e){
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
        }
    }
}
