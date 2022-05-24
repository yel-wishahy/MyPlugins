package shallowcraft.itemeconomy.ThirdPartyIntegration.smartshop;

import java.util.List;

public class SmartShopConfig_old {
    public static int minP = 70;
    public static int maxP = 110;

    public static int minQ = 5;
    public static int maxQ = 80;

    public static double earningFactor = 0.20;

    public static double michaelFactor = 0.50;
    public static String michaelName = "SingleGear124";

    public static int nextOrderHours = 32;

    public static String smartShopHolderName = "CHINA";


    public static final String dataFileName = "shopOrderData";
    public static final String logFileName = "shopOrderLogs";
    public static List<String> subCommands = List.of("info", "accept", "decline", "remove", "generate", "log", "save", "reload", "search");
    public static List<String> aliases = List.of("smartShop", "ss", "s", "SmartShop");
    public static String command = "SmartShop";


    public static int maxAllowedInitializeAttempts = 5;
    public static long initializeTaskDelay = 200; //ticks
}
