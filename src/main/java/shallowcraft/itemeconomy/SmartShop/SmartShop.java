package shallowcraft.itemeconomy.SmartShop;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.maxgamer.quickshop.api.QuickShopAPI;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Data.DataSerializer;
import shallowcraft.itemeconomy.Data.InvalidDataException;
import shallowcraft.itemeconomy.ItemEconomy;
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


    private SmartShop() {
        instance = this;

        Plugin plugin = Bukkit.getPluginManager().getPlugin("QuickShop");
        if(plugin != null){
            QuickShopAPI = (QuickShopAPI)plugin;
            ItemEconomy.log.info("Successfully loaded QuickShop api");
            isEnabled = true;
        } else {
            ItemEconomy.log.info("failed to load QuickShop api");
            isEnabled = false;
        }

        holder = SmartShopUtil.getSmartShopDeposit();

        if(holder == null)
            isEnabled = false;
        else {
            ItemEconomy.log.info("loading data for smart shop");
            loadData();
        }

        log = new ShopOrderLog();
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
            File dataFile = DataSerializer.createDataFile(SmartShopConfig.dataFileName);
            DataSerializer.saveShopOrdersToJSON(shopOrders, dataFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            ItemEconomy.log.info("[ItemEconomy Smart Shop] Failed to save data.");
            return false;
        }
    }

    public boolean loadData() {
        try {
            File dataFile = DataSerializer.getDataFile(SmartShopConfig.dataFileName);
            if (dataFile.exists())
               shopOrders = DataSerializer.loadShopOrdersFromJSON(dataFile);
            else
                shopOrders = new HashMap<>();

            return true;
        } catch (IOException | InvalidDataException e) {
            e.printStackTrace();
            shopOrders = new HashMap<>();
            ItemEconomy.log.info("[ItemEconomy Smart Shop] Failed to load data");
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
