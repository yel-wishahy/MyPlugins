package shallowcraft.itemeconomy.SmartShop;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.api.QuickShopAPI;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Data.DataManager;
import shallowcraft.itemeconomy.Data.InvalidDataException;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.SmartShop.Commads.SmartShopCommand;
import shallowcraft.itemeconomy.SmartShop.Commads.SmartShopTabCompleter;
import shallowcraft.itemeconomy.SmartShop.Listener.SSEventHandler;
import shallowcraft.itemeconomy.SmartShop.ShopOrder.ShopOrder;
import shallowcraft.itemeconomy.SmartShop.ShopOrder.ShopOrderLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartShop {
    private static SmartShop instance;
    @Getter private boolean isEnabled;

    @Getter @Setter private Map<String, List<ShopOrder>> shopOrders;
    @Getter @Setter private Account holder;
    @Getter @Setter private ShopOrderLog log;
    @Getter @Setter public QuickShopAPI QuickShopAPI;

    private int initializeAttempt = 0;


    private SmartShop() {
        instance = this;
        isEnabled = false;
    }

    public void initializeSmartShop() {
        initializeAttempt++;
        Server server = Bukkit.getServer();

        if (server.getPluginManager().isPluginEnabled("QuickShop")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickShop");

            if (plugin != null) {
                QuickShopAPI = (QuickShopAPI) plugin;
                holder = SmartShopUtil.getSmartShopDeposit();

                loadData();

                log = new ShopOrderLog();
                log.loadPreviousLogs();

                ItemEconomyPlugin.getInstance().getCommand(SmartShopConfig.command).setExecutor(new SmartShopCommand());
                server.getPluginCommand(SmartShopConfig.command).setTabCompleter(new SmartShopTabCompleter());
                server.getPluginManager().registerEvents(new SSEventHandler(), ItemEconomyPlugin.getInstance());


                ItemEconomy.log.info("[ItemEconomy: SmartShop] Successfully loaded SmartShop");

                isEnabled = true;
            }
        }

        if(!isEnabled && initializeAttempt < SmartShopConfig.maxAllowedInitializeAttempts){
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
                public void run() {
                    SmartShop.getInstance().initializeSmartShop();
                }
            };
            task.runTaskLater(ItemEconomyPlugin.getInstance(), SmartShopConfig.initializeTaskDelay);
            ItemEconomy.log.info("[ItemEconomy: SmartShop] Failed to load SmartShop, will attempt again in 200 ticks");
        }
    }

    public static SmartShop getInstance() {
        if(instance == null)
            instance = new SmartShop();

        return instance;
    }

    public boolean saveData() {
        cleanOrders();
        ShopOrderLog.getInstance().saveLogs();
        try {
            File dataFile = DataManager.createDataFile(SmartShopConfig.dataFileName);
            DataManager.saveShopOrdersToJSON(shopOrders, dataFile);
            return true;
        } catch (IOException e) {
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
            ItemEconomy.log.info("[ItemEconomy Smart Shop] Failed to save data.");
            return false;
        }
    }

    public boolean loadData() {
        try {
            File dataFile = DataManager.getDataFile(SmartShopConfig.dataFileName);
            if (dataFile.exists())
               shopOrders = DataManager.loadShopOrdersFromJSON(dataFile);
            else
                shopOrders = new HashMap<>();

            return true;
        } catch (IOException | InvalidDataException e) {
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
            shopOrders = new HashMap<>();
            ItemEconomy.log.info("[ItemEconomy: Smart Shop] Failed to load data");
            return false;
        }
    }

    public void cleanOrders(){
        //ItemEconomy.log.info("cleaning orders");
        for (List<ShopOrder> orderData:shopOrders.values()) {
            //ItemEconomy.log.info("cleaning orders " + orderData.size());
            boolean result = orderData.removeIf(ShopOrder::isStale);
            //ItemEconomy.log.info("cleaned " + result + " " + orderData.size());

        }
    }

    public void cleanOrders(String id){
        if(shopOrders.containsKey(id)){
            //ItemEconomy.log.info("cleaning orders " + shopOrders.get(id).size());
            boolean result =  shopOrders.get(id).removeIf(ShopOrder::isStale);
            //ItemEconomy.log.info("cleaned " + result + " " + shopOrders.get(id).size());
        }

    }


    public void generateOrderAll(){
        Map<String, ShopOrder> orders = SmartShopUtil.generateShopOrders();

        for (String id:orders.keySet()) {
            ShopOrder o = orders.get(id);
            if(!shopOrders.containsKey(id))
                shopOrders.put(id, new ArrayList<>());
            shopOrders.get(id).add(o);
        }

        saveData();
    }

    public void generateMultipleOrdersAll(int amount){
        cleanOrders();
        for (int i = 0; i < amount ; i++)
            generateOrderAll();
    }

    public void generateOrderForID(String id){
        ShopOrder order = SmartShopUtil.repeatGenerateRandomOrder(SmartShopUtil.getShopByAccountID(),holder,id);

        if(order != null){
            if(!shopOrders.containsKey(id))
                shopOrders.put(id, new ArrayList<>());

            shopOrders.get(id).add(order);
        }
    }

    public void generateMultipleOrdersForID(String id, int amount){
        cleanOrders();
        for (int i = 0; i < amount ; i++)
            generateOrderForID(id);
    }

}
