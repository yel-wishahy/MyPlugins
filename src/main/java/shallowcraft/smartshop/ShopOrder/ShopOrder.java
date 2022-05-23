package shallowcraft.smartshop.ShopOrder;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.BankVault.VaultType;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Data.DataUtil;
import shallowcraft.itemeconomy.Data.Serializable;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.smartshop.SmartShopUtil;
import shallowcraft.itemeconomy.Transaction.TransactionResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ShopOrder implements Serializable<ShopOrder> {
    @Getter @Setter
    private ItemStack orderItem;
    @Getter @Setter
    private int orderQuantity;
    @Getter @Setter
    private double costPerQuantity;
    @Getter @Setter
    private ShopOrderType orderType;
    @Getter @Setter
    private Account buyer;
    @Getter @Setter
    private Account seller;
    @Getter @Setter
    private Shop quickShop;
    @Getter @Setter
    private Date orderTime;
    @Getter @Setter
    private boolean isOrderComplete;
    @Getter @Setter
    private ShopOrderResult result;
    @Setter
    private boolean isStale = false;

    public ShopOrder(Map<String, String> inputData) {
        try {
            //ItemEconomy.log.info("loading shop order with inputdata: " + inputData);
            String[] orderitem = (inputData.get("Order Item").split(","));
            orderItem = new ItemStack(Objects.requireNonNull(Material.getMaterial(orderitem[0])), Integer.parseInt(orderitem[1]));

            String[] locData = inputData.get("Shop Location").split(",");
            Location loc = DataUtil.deserializeLocation(locData);
            //ItemEconomy.log.info("load loc success");
            assert QuickShop.getInstance().getShopManager().getShop(loc) != null;
            quickShop = QuickShop.getInstance().getShopManager().getShop(loc);


            buyer = ItemEconomy.getInstance().getAccounts().get(inputData.get("Buyer"));
            //ItemEconomy.log.info("load buyer success");
            seller = ItemEconomy.getInstance().getAccounts().get(inputData.get("Seller"));
            //ItemEconomy.log.info("load seller success");

            orderQuantity = Integer.parseInt(inputData.get("Quantity"));
            // ItemEconomy.log.info("load quantity success");
            costPerQuantity = Double.parseDouble(inputData.get("Price"));
            //ItemEconomy.log.info("load price success");

            orderTime = shallowcraft.itemeconomy.Config.shortTimeFormat.parse(inputData.get("Time"));
            //ItemEconomy.log.info("load time success");

            orderType = ShopOrderType.getType(Integer.parseInt(inputData.get("Order Type")));
            //ItemEconomy.log.info("load type success");

            isOrderComplete = Boolean.parseBoolean(inputData.get("Order Success"));
            //ItemEconomy.log.info("load completion status success");

            result = null;
            //ItemEconomy.log.info("load success");
        } catch (Exception e) {
            e.printStackTrace();
            isStale = true;
        }

    }

    public ShopOrder(Shop shop, Account buyer, Account seller, ItemStack orderItem, int orderQuantity, double costPerQuantity, ShopOrderType orderType) {
        this.orderItem = orderItem;
        this.quickShop = shop;
        this.buyer = buyer;
        this.seller = seller;
        this.orderQuantity = orderQuantity;
        this.costPerQuantity = costPerQuantity;
        this.orderType = orderType;
        this.orderTime = new Date();
        this.isOrderComplete = false;
        this.result = null;
        this.isStale = false;
    }

    public ShopOrderResult executeShopOrder() {
        double subtotal = orderQuantity * costPerQuantity;

        if (quickShop.getRemainingStock() >= orderQuantity && buyer.getBalance(VaultType.ALL) >= subtotal) {
            buyer.withdraw((int) Math.round(subtotal),VaultType.ALL);
            TransactionResult r = ItemEconomy.getInstance().deposit(seller.getID(), subtotal);

            Inventory inv = ((Container) quickShop.getLocation().getBlock().getState()).getInventory();

            {
                SmartShopUtil.removeItemStacks(orderItem, orderQuantity, inv);

                isOrderComplete = true;

                ShopOrderResult result = new ShopOrderResult(r, ShopOrderResult.ShopOrderResultType.SUCCESS, orderQuantity, orderItem);
                this.result = result;
                ShopOrderLog.getInstance().log(this);
                return result;
            }
        }

        this.isStale = true;
        ShopOrderResult.ShopOrderResultType type = ShopOrderResult.ShopOrderResultType.FAILURE;
        if(quickShop.getRemainingStock() < orderQuantity)
            type = ShopOrderResult.ShopOrderResultType.INSUFFICIENT_STOCK;
        if(buyer.getBalance(VaultType.ALL) < orderQuantity * costPerQuantity)
            type = ShopOrderResult.ShopOrderResultType.BUYER_IS_BROKE;


        ShopOrderResult result = new ShopOrderResult(new TransactionResult(0, TransactionResult.ResultType.FAILURE,
                "failed order"), type, 0, orderItem);
        this.result = result;
        ShopOrderLog.getInstance().log(this);
        return result;
    }

    @Override
    public Map<String, String> getSerializableData() {
        Map<String, String> outputData = new HashMap<>();

        String itemData = orderItem.getType() + "," + orderItem.getAmount();
        outputData.put("Order Item", itemData);

        String shopLoc = DataUtil.serializeLocation(quickShop.getLocation());
        outputData.put("Shop Location", shopLoc);

        outputData.put("Buyer", buyer.getID());
        outputData.put("Seller", seller.getID());

        outputData.put("Quantity", String.valueOf(orderQuantity));
        outputData.put("Price", String.valueOf(costPerQuantity));

        outputData.put("Time", shallowcraft.itemeconomy.Config.shortTimeFormat.format(orderTime));

        outputData.put("Order Type", String.valueOf(orderType.getId()));

        outputData.put("Order Success", String.valueOf(isOrderComplete));

        return outputData;
    }

    public enum ShopOrderType {
        BUY(1),
        SELL(2);

        private int id;

        ShopOrderType(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }

        public static ShopOrderType getType(int id) {
            if (id == 2) {
                return SELL;
            }
            return BUY;
        }
    }

    public static class ShopOrderResult {
        public final TransactionResult transactionResult;
        public final ShopOrderResultType resultType;
        public final int amount;
        public final ItemStack itemStack;
        public final String timeOfResult;

        public ShopOrderResult(TransactionResult transactionResult, ShopOrderResultType resultType, int amount, ItemStack itemStack) {
            this.transactionResult = transactionResult;
            this.resultType = resultType;
            this.amount = amount;
            this.itemStack = itemStack;
            this.timeOfResult = shallowcraft.itemeconomy.Config.shortTimeFormat.format(new Date());
        }

        public enum ShopOrderResultType {
            SUCCESS(1),
            FAILURE(2),
            INSUFFICIENT_STOCK(3),
            BUYER_IS_BROKE(4);


            private int id;

            ShopOrderResultType(int id) {
                this.id = id;
            }

            int getId() {
                return id;
            }

            public static ShopOrderResultType getType(int id) {
                if (id == 2) {
                    return FAILURE;
                }
                return SUCCESS;
            }
        }

        @Override
        public String toString() {
            return ChatColor.GREEN + "Order Result: " + ChatColor.YELLOW + resultType.toString() + ", " + ChatColor.GREEN +
                    " amount deposited in account for the sale of " + ChatColor.YELLOW + amount * itemStack.getAmount() + " x " + ChatColor.AQUA + itemStack.getType()
                    + ChatColor.GREEN + " is " + ChatColor.YELLOW + transactionResult.amount + ChatColor.AQUA + " Diamonds!";
        }

        public String getLogResult(){
            return "* " + timeOfResult + " Order Result: " + resultType.toString() + " for "
                    + transactionResult.amount + " diamond  SALE of " + amount * itemStack.getAmount() + " x " + itemStack.getType();
        }
    }

    @Override
    public String toString() {
        int quantity = orderItem.getAmount() * orderQuantity;
        String time = shallowcraft.itemeconomy.Config.shortTimeFormat.format(orderTime);

        return ChatColor.GOLD + "* " + time + " " + ChatColor.GREEN + orderType.toString() + " Order for " + ChatColor.GOLD + seller.getName() + ChatColor.GREEN +
                " :\n " + ChatColor.YELLOW + quantity + " x " +
                ChatColor.AQUA + orderItem.getType() + ChatColor.GREEN + " for " + ChatColor.YELLOW + (orderQuantity * costPerQuantity) +
                ChatColor.AQUA + " Diamonds.";
    }

    public String getInfo(){
        return orderType.toString() + "_" + ( orderItem.getAmount() * orderQuantity) + "_" + orderItem.getType();
    }

    public boolean timerSurpassed(){
       Date next = DateUtils.addHours(orderTime, Config.nextOrderHours);
       Date now = new Date();

       return now.compareTo(next) > 0;
    }

    public int getTotal(){
        return (int) (costPerQuantity * orderQuantity);
    }

    public boolean isStale(){
        return timerSurpassed() || isOrderComplete || isStale;
    }
}
