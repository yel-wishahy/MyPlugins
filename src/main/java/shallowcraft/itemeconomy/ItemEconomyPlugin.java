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
import shallowcraft.itemeconomy.ThirdPartyIntegration.jobs.Listener.JobsEventHandler;
import shallowcraft.itemeconomy.ThirdPartyIntegration.smartshop.SmartShop;
import shallowcraft.itemeconomy.VaultEconomyHook.Economy_ItemEconomy;

import java.util.logging.Logger;

//plugin class for item economy that extends JavaPlugin, handles actual startup and communication with other plugins
public class ItemEconomyPlugin extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    public final static String name = "ItemEconomy";
    @Getter @Setter
    private static ItemEconomyPlugin instance;
    @Getter @Setter
    private ItemEconomy ItemEconomy;
    @Getter @Setter
    private SmartShop SmartShop;
    @Getter @Setter boolean taxesEnabled;


    @Override
    public void onEnable() {
        instance = this;

        try{
            Config.loadConfig();
            log.info("[ItemEconomy] Successfully loaded config.yml");
        } catch (Exception e) {
            if((boolean) Config.ItemEconomyConfig.get("defaultDebug"))
                e.printStackTrace();
            log.info("[ItemEconomy] Failed to load config, check if file exists. Or create config with /ie createconfig.");
        }

        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else
            log.info("[ItemEconomy] Vault hook successful.");

        ItemEconomy.loadData();

        registerIEEventHandler();
        registerCommands();

        if((boolean) Config.ItemEconomyConfig.get("enableSmartShop"))
            try {
                setupSmartShop();
            } catch (Exception e){
                e.printStackTrace();
            }

        if((boolean) Config.ItemEconomyConfig.get("enableTaxes"))
            try {
                setupTaxes();
                taxesEnabled = true;
            } catch (Exception e){
                taxesEnabled = false;
                e.printStackTrace();
            }

        if((boolean) Config.ItemEconomyConfig.get("enableJobsIntegration"))
            setupJobs();
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

    //quasi third party integration : QuickShop
    private void setupSmartShop(){
        SmartShop = shallowcraft.itemeconomy.ThirdPartyIntegration.smartshop.SmartShop.getInstance();
        SmartShop.initializeSmartShop();
    }

    //third party plugin integration : Jobs
    private void setupJobs(){
        getServer().getPluginManager().registerEvents(new JobsEventHandler(), this);
    }

    private void setupTaxes(){
        try {
            this.getCommand(Config.TaxCommand).setExecutor(new TaxCommand());
            getServer().getPluginCommand(Config.TaxCommand).setTabCompleter(new TaxTabCompleter());
        } catch (Exception ignored) {
            log.info("[ItemEconomy] Failed to setup tax system!");
        }
    }

    private void registerIEEventHandler() {
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
