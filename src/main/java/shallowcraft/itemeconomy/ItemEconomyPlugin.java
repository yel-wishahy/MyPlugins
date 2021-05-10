package shallowcraft.itemeconomy;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import shallowcraft.itemeconomy.Commands.IECommand;
import shallowcraft.itemeconomy.Commands.IETabCompleter;
import shallowcraft.itemeconomy.Tax.command.TaxCommand;
import shallowcraft.itemeconomy.Tax.command.TaxTabCompleter;
import shallowcraft.itemeconomy.Listener.IEEventHandler;
import shallowcraft.itemeconomy.SmartShop.Commads.SmartShopCommand;
import shallowcraft.itemeconomy.SmartShop.Commads.SmartShopTabCompleter;
import shallowcraft.itemeconomy.SmartShop.Listener.SSEventHandler;
import shallowcraft.itemeconomy.SmartShop.SmartShop;
import shallowcraft.itemeconomy.SmartShop.SmartShopConfig;
import shallowcraft.itemeconomy.VaultEconomyHook.Economy_ItemEconomy;

import java.util.logging.Logger;

public class ItemEconomyPlugin extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    public final static String name = "ItemEconomy";
    @Getter @Setter
    private static ItemEconomyPlugin instance;
    @Getter @Setter
    private ItemEconomy ItemEconomy;
    @Getter @Setter
    private SmartShop SmartShop;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerEventHandler();
        registerCommands();

        if(Config.enableSmartShop)
            setupSmartShop();

        if(Config.enableTaxes)
            setupTaxes();
    }

    @Override
    public void onDisable() {
        ItemEconomy.saveData();
        if(SmartShop.isEnabled())
            SmartShop.saveData();
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            ItemEconomy = shallowcraft.itemeconomy.ItemEconomy.getInstance();
            getServer().getServicesManager().register(Economy.class, new Economy_ItemEconomy(), this, ServicePriority.Normal);
            return true;
        } else {
            return false;
        }
    }

    private void setupSmartShop(){
        try{
            SmartShop = shallowcraft.itemeconomy.SmartShop.SmartShop.getInstance();
            this.getCommand(SmartShopConfig.command).setExecutor(new SmartShopCommand());
            getServer().getPluginCommand(SmartShopConfig.command).setTabCompleter(new SmartShopTabCompleter());
            getServer().getPluginManager().registerEvents(new SSEventHandler(), this);

            if(!SmartShop.isEnabled())
                SmartShop = null;
        } catch (Exception e){
            e.printStackTrace();
            log.info("[ItemEconomy] Failed to setup smart shop system!");
        }
    }

    private void setupTaxes(){
        try {
            this.getCommand(Config.TaxCommand).setExecutor(new TaxCommand());
            getServer().getPluginCommand(Config.TaxCommand).setTabCompleter(new TaxTabCompleter());
        } catch (Exception ignored) {
            log.info("[ItemEconomy] Failed to setup tax system!");
        }
    }

    private void registerEventHandler() {
        getServer().getPluginManager().registerEvents(new IEEventHandler(), this);
    }

    private void registerCommands() {
        try {
            this.getCommand(Config.IECommand).setExecutor(new IECommand());
            getServer().getPluginCommand(Config.IECommand).setTabCompleter(new IETabCompleter());
        } catch (Exception ignored) {
            log.info("[ItemEconomy] Failed to setup item economy");
        }
    }
}
