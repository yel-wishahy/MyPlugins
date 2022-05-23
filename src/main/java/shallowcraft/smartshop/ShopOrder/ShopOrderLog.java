package shallowcraft.smartshop.ShopOrder;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Data.DataManager;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopOrderLog{
    @Getter @Setter
    private Map<String, List<String>> orderLogs;

    @Getter @Setter
    private static ShopOrderLog instance;

    public ShopOrderLog(){
        instance = this;
    }

    public void log(ShopOrder order){
        String id = order.getSeller().getID();
        String log = order.getResult().getLogResult();

        if(!orderLogs.containsKey(id))
            orderLogs.put(id, new ArrayList<>());

        orderLogs.get(id).add(log);
        saveLogs();
    }

    public ItemStack getLogBook(String id){
        if(orderLogs.containsKey(id)){
            ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
            bookMeta.setTitle("OrderLogs");
            bookMeta.setAuthor("Smart Shop - Log book of " + Util.getPlayerName(id));

            List<String> pages = new ArrayList<>();
            pages.add("Order Logs For " + Util.getPlayerName(id));

            StringBuilder page = new StringBuilder();

            for(String log: orderLogs.get(id)){
                page.append(log).append("\n");
                pages.add(page.toString());
                page = new StringBuilder();
            }

            bookMeta.setPages(pages);

            writtenBook.setItemMeta(bookMeta);

            return writtenBook;
        }

        return null;
    }



    public boolean loadPreviousLogs(){
        try{
            File dataFile = DataManager.getDataFile(Config.SSlogFileName);
            if(dataFile.exists())
                orderLogs = DataManager.loadShopOrderLogsFromJSON(dataFile);
            else
                orderLogs = new HashMap<>();
            return true;
        } catch (Exception e){
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
            orderLogs = new HashMap<>();
            ItemEconomy.log.info("[ItemEconomy Smart Shop] Failed to load order logs");
            return false;
        }
    }

    public boolean saveLogs(){
        try {
            File dataFile = DataManager.createDataFileJSON(Config.SSlogFileName);
            DataManager.saveShopOrderLogsToJSON(orderLogs, dataFile);
            return true;
        } catch (Exception e) {
            if(ItemEconomy.getInstance().isDebugMode())
                e.printStackTrace();
            ItemEconomy.log.info("[ItemEconomy Smart Shop] Failed to save order logs.");
            return false;
        }
    }
}
