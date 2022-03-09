package shallowcraft.itemeconomy.SmartShop;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.maxgamer.quickshop.api.shop.Shop;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.SmartShop.ShopOrder.ShopOrder;
import shallowcraft.itemeconomy.Util.Util;

import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class SmartShopUtil {
    public static Random rand = new Random();
    private static List<String> Materials;

    public static Map<String, List<Shop>> getShopByAccountID(){
        //this is probably bad practice since this assert mutates instance of smartshop
        assert SmartShop.getInstance().QuickShopAPI != null;
        List<Shop> shops = SmartShop.getInstance().QuickShopAPI.getShopManager().getAllShops();
        Map<String, List<Shop>> outputMap = new HashMap<>();

        for (Shop shop:shops) {
            String id = shop.getOwner().toString();
            if(ItemEconomy.getInstance().hasAccount(id)){
                if(!outputMap.containsKey(id)){
                    outputMap.put(id, new ArrayList<>());
                    outputMap.get(id).add(shop);
                } else{
                    outputMap.get(id).add(shop);
                }
            }
        }

        return outputMap;
    }

    public static int getRandomBuyQuantity(int currentQuantity){
        int randomNum = rand.nextInt((SmartShopConfig.maxQ - SmartShopConfig.minQ) + 1) + SmartShopConfig.minQ;
        return (int) (((double) randomNum)/100.0 * currentQuantity);
    }

    public static double getRandomBuyPrice(double currentPrice){
        int randomNum = rand.nextInt((SmartShopConfig.maxP - SmartShopConfig.minP) + 1) + SmartShopConfig.minP;
        return ((double) randomNum)/100.0 * currentPrice;
    }

    public static Shop getRandomShop(List<Shop> shops){
        int min = 0;
        int max = shops.size() - 1;

        int randomNum = rand.nextInt((max - min) + 1) + min;
        return shops.get(randomNum);
    }

    public static Map<String, ShopOrder> generateShopOrders(){
        Map<String, List<Shop>> accountShops = getShopByAccountID();
        Map<String, ShopOrder> shopOrders = new HashMap<>();
        Account buyer = SmartShop.getInstance().getHolder();

        for (String id:accountShops.keySet()) {
            ShopOrder order = repeatGenerateRandomOrder(accountShops, buyer, id);

            if(order != null)
                shopOrders.put(id, order);
        }

        return shopOrders;
    }

    public static ShopOrder generateRandomOrder(Map<String, List<Shop>> accountShops, Account buyer, String id ){
        Account seller = ItemEconomy.getInstance().getAccounts().get(id);
        Shop shop = getRandomShop(accountShops.get(id));
        if(seller != null && shop != null){
            double price = getRandomBuyPrice(shop.getPrice());
            int quantity = getRandomBuyQuantity(shop.getRemainingStock());
            ShopOrder order = new ShopOrder(shop, buyer, seller, shop.getItem(), quantity, price, ShopOrder.ShopOrderType.BUY);
            balanceShopOrder(order);

            if(!order.isStale())
                return order;
        }

        return null;
    }

    public static ShopOrder repeatGenerateRandomOrder(Map<String, List<Shop>> accountShops, Account buyer, String id){
        int retryCount = accountShops.get(id).size();

        for(int i = 0; i <= retryCount; i++){
            ShopOrder order = generateRandomOrder(accountShops, buyer, id);
            if(order!=null)
                return order;
        }

        return null;
    }


    public static Account getSmartShopDeposit(){
        if(!ItemEconomy.getInstance().getAccounts().containsKey(SmartShopConfig.smartShopHolderName))
            createSmartShopDeposit();

        return ItemEconomy.getInstance().getAccounts().get(SmartShopConfig.smartShopHolderName);
    }

    public static List<String> getOrderSummaries(String id){
        List<String> summary = new ArrayList<>();

        for (ShopOrder s:SmartShop.getInstance().getShopOrders().get(id)) {
            summary.add(s.getInfo());
        }

        return summary;
    }

    public static String getOrdersInfoMessage(String id){
        StringBuilder msg = new StringBuilder();
        String name = Util.getPlayerName(id);
        msg.append(ChatColor.GOLD).append("[SmartShop] ").append(ChatColor.GREEN).append(" Order info for ").
                append(ChatColor.YELLOW).append(name).append(": \n");
        msg.append(ChatColor.GREEN).append(" Potential Earnings: ").append(ChatColor.YELLOW).append(getPotentialOrderEarnings(id))
                .append(ChatColor.AQUA).append(" Diamonds.\n");

        if(SmartShop.getInstance().getShopOrders().containsKey(id)){
            for (ShopOrder s:SmartShop.getInstance().getShopOrders().get(id)) {
                msg.append(s.toString()).append("\n");
            }

            if(SmartShop.getInstance().getShopOrders().get(id).isEmpty())
                msg.append(ChatColor.GRAY).append( "No Shop Orders. New orders will be listed tomorrow.");
        } else {
            msg.append(ChatColor.GRAY).append( "No Shop Orders. New orders will be listed tomorrow.");
        }




        return msg.toString();
    }

    public static ShopOrder getOrderFromSummary(List<ShopOrder> shopOrders, String summary){
        for (ShopOrder s: shopOrders) {
            if (s.getInfo().equals(summary))
                return s;
        }

        return null;
    }

    public static void createSmartShopDeposit(){
        ItemEconomy.getInstance().getAccounts().put(SmartShopConfig.smartShopHolderName, new GeneralAccount(SmartShopConfig.smartShopHolderName));
    }

    public static void declineOrder(ShopOrder order){
        String id = order.getSeller().getID();
        SmartShop.getInstance().getShopOrders().get(id).remove(order);
        SmartShop.getInstance().saveData();
    }

    public static void declineAllOrders(String id){
        SmartShop.getInstance().getShopOrders().remove(id);
        SmartShop.getInstance().getShopOrders().put(id, new ArrayList<>());
        SmartShop.getInstance().saveData();
    }

    public static void balanceShopOrder(ShopOrder order){
        int buyerBalance = order.getBuyer().getBalance();
        int potential = getPotentialOrderEarnings(order.getSeller().getID());
        int maxEarnable = (int) (Util.getMedianPlayerBalance() * SmartShopConfig.earningFactor);

        if(order.getTotal() + potential > maxEarnable){
            int correction = maxEarnable - potential;
            if(correction < 0)
                correction = 0;

            reduceOrderTotal(order, correction);
        }

        if(order.getTotal() > buyerBalance/Util.getPlayerAccountCount()){
            reduceOrderTotal(order, buyerBalance/Util.getPlayerAccountCount());
        }

        if(order.getOrderQuantity() > order.getQuickShop().getRemainingStock()){
            reduceOrderTotal(order, order.getQuickShop().getRemainingStock());
        }

        if(order.getOrderQuantity() <= 0 || order.getCostPerQuantity() <= 0){
            order.setStale(true);
        }
    }

    private static void reduceOrderTotal(ShopOrder order, int reduceTo){
        int idealQuantity = (int) (reduceTo/order.getCostPerQuantity());
        order.setOrderQuantity(idealQuantity);
    }

    private static int getPotentialOrderEarnings(String id){
        Map<String, List<ShopOrder>> shopOrders = SmartShop.getInstance().getShopOrders();
        int potential = 0;

        if(shopOrders.containsKey(id)){
            for (ShopOrder order:shopOrders.get(id)) {
                potential+=order.getOrderQuantity()*order.getCostPerQuantity();
            }
        }

        return potential;
    }

    private static void antiMichael(ShopOrder order){
        String name = SmartShopConfig.michaelName;
        String id = Util.getPlayerID(name);

        int subtotal = (int) (order.getOrderQuantity() * order.getCostPerQuantity());
        int total = subtotal + getPotentialOrderEarnings(id);

        int playersNum = 0;

        order.setOrderQuantity((int) (order.getOrderQuantity() * SmartShopConfig.michaelFactor));

        int totalEarning = 0;
        for (String uuid:SmartShop.getInstance().getShopOrders().keySet()) {
            if(!uuid.equals(id) && getPotentialOrderEarnings(uuid) >= 5){
                totalEarning += getPotentialOrderEarnings(uuid);
                playersNum++;
            }
        }

        int avgEarning = 0;
        if(playersNum > 0)
            avgEarning = totalEarning/(playersNum);

        if(total > avgEarning && avgEarning > 5)
            reduceOrderTotal(order, avgEarning);
    }

    public static void removeItemStacks(ItemStack item, int quantity, Inventory inv){
        int removed = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            if(removed >= quantity)
                break;
            ItemStack invItem = inv.getItem(i);
            if(invItem != null && invItem.getType().equals(item.getType()) && invItem.getAmount() >= item.getAmount()){
                if(invItem.getAmount() == item.getAmount())
                    inv.setItem(i, null);
                else
                    inv.setItem(i, new ItemStack(item.getType(), invItem.getAmount() - item.getAmount()));

                removed++;
            }
        }
    }

    public static boolean hasPendingOrders(String id){
        SmartShop.getInstance().cleanOrders(id);
        return SmartShop.getInstance().getShopOrders().containsKey(id)
                && !SmartShop.getInstance().getShopOrders().get(id).isEmpty();
    }

    public static String getJoinMessage(String id){
        SmartShop.getInstance().cleanOrders(id);
        int numOrders = SmartShop.getInstance().getShopOrders().get(id).size();

        return ChatColor.GOLD + "[Smart Shop]" + ChatColor.YELLOW + " You have " + ChatColor.AQUA + numOrders
                + ChatColor.YELLOW  + " pending BUY orders from " + ChatColor.RED + ChatColor.BOLD + "CHINA\n" + ChatColor.RESET +
                ChatColor.YELLOW + " Check with:  " + ChatColor.GOLD+ "\\s info" +
                ChatColor.YELLOW+ " and Accept or Decline with: " + ChatColor.GOLD +
                "\\s accept <order name> OR \\s decline <order name>.";
    }

    public static List<String> getMaterialsListAsString(){
        if(Materials == null){
            Materials = new ArrayList<>();
            for (Material mat:Material.values()) {
                Materials.add(mat.toString());
            }
        }

        return  Materials;
    }
    public static List<Material> getSimilarMaterials(String itemNameArg){
        List<String> completions = new ArrayList<>();
        List<Material> matches = new ArrayList<>();
        StringUtil.copyPartialMatches(itemNameArg, SmartShopUtil.getMaterialsListAsString(), completions);

        for (String matName:completions) {
            matches.add(Material.matchMaterial(matName));
        }

        return matches;
    }

    public static List<Shop> findAllShopsForSimilarMaterial(List<Material> materials){
        List<Shop> output = new ArrayList<>();

        for (Shop shop: Objects.requireNonNull(SmartShop.getInstance().QuickShopAPI.getShopManager()).getAllShops()){
            if(materials.contains(shop.getItem().getType()))
                output.add(shop);
        }

        return output;
    }

    public static String getSearchResultsMessage(Location searchStartLoc, String itemArg){
        List<Shop> shops = findAllShopsForSimilarMaterial(getSimilarMaterials(itemArg));
        return getAllShopLocationMessage(searchStartLoc, shops);
    }

    public static String getAllShopLocationMessage(Location searchStartLoc, List<Shop> shops){
        StringBuilder msg = new StringBuilder();
        msg.append(ChatColor.GOLD ).append("[Smart Shop] ").append(ChatColor.GREEN).append("Shop Search Results: \n");

        for (Shop shop:shops) {
            msg.append(getShopLocationMessage(searchStartLoc, shop)).append("\n");
        }

        return msg.toString();
    }

    public static String getShopLocationMessage(Location searchStartLoc, Shop shop){
        String direction = getDirectionMessage(searchStartLoc, shop.getLocation());
        String itemType = shop.getItem().getType().toString().toLowerCase();
        String cost = (new DecimalFormat("#.##")).format(shop.getPrice());
        String sellerName = Util.getPlayerName(shop.getOwner().toString());
        String saleType = shop.getShopType().toString();
        return "* " + ChatColor.GOLD + sellerName + ChatColor.GREEN + " is " + ChatColor.YELLOW + saleType + ChatColor.AQUA + " "
                + itemType + ChatColor.GREEN + " for " + ChatColor.YELLOW + cost + ChatColor.AQUA + " Diamond "
                + ChatColor.GOLD + "( " + ChatColor.GREEN + direction + ChatColor.GOLD + " ).";
    }

    public static String getDirectionMessage(Location start, Location finish){
        if(!start.getWorld().getEnvironment().equals(finish.getWorld().getEnvironment()))
            return "Cannot calculate distance to " + finish.getWorld().getName();

        String distance = (new DecimalFormat("#.##")).format(start.distance(finish));
        String direction = " ";
        int difX = finish.getBlockX() - start.getBlockX();
        int difZ = finish.getBlockZ() - start.getBlockZ();

        if(difZ > 0){
            if(difX > 0)
                direction = "North - East";
            else if (difX < 0)
                direction = "North - West";
            else
                direction = "North";
        } else{
            if(difX > 0)
                direction = "South- East";
            else if (difX < 0)
                direction = "South - West";
            else
                direction = "South";
        }

        return distance + " Blocks " + direction;
    }






}
