package shallowcraft.itemeconomy;

import org.bukkit.Material;
import org.yaml.snakeyaml.Yaml;
import shallowcraft.itemeconomy.Data.DataManager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

//should probably move this to json, but some things like command aliases etc shouldnt be configurable
public class Config {
    public static Map<String, Object> ItemEconomyConfig;
    public static Map<String, Object> TaxesConfig;
    public static Map<String, Object> SmartShopConfig;

    /**  FINAL*/
    //data file names
    public static final String configFileName = "config";
    public static final String IEdataFileName = "economyData";
    public static final String SSdataFileName = "shopOrderData";
    public static final String SSlogFileName = "shopOrderLogs";

    //time formates
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
    public static final SimpleDateFormat shortTimeFormat = new SimpleDateFormat("MM.dd 'at' HH:mm");

    //vaults/accounts
    public static final String vaultHeader = "[Vault]";
    public static final List<String> transferTypes = List.of("Deposit-Vault", "Withdraw-Vault", "Regular-Vault");
    public static final List<String> accountTypes = List.of("Player Account", "General Account");
    public static HashSet<Material> VaultContainerTypes = new HashSet<>(List.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL));

    //commands
    public static final List<String> IESubCommands = List.of("create_account",
            "balance", "list_accounts", "create_account_all", "remove_account",
            "reload", "save", "load", "baltop", "admindeposit", "adminwithdraw", "withdraw","deposit",
            "transfer", "admintransfer", "statsupdate", "debug","createconfig");

    public static final List<String> IECommandAliases = List.of("ItemEconomy", "itemeconomy",
            "IE", "ie", "eco", "money");

    public static final List<String> TaxCommandAliases = List.of("tax", "ietax", "ieTax", "taxes", "t");
    public static final List<String> TaxSubCommands = List.of("add", "addcustom", "remove", "tax", "taxall", "info", "clear", "edit", "redistribute", "taxprofits", "resetprofits");
    public static final List<String> TaxEditSubCommands = List.of("timeset_now", "set_rate");
    public static final List<String> IEShopCommandAliases = List.of("Shop", "ItemEconomyShop", "itemeconomyshop", "ieshop", "shop", "ss", "buy", "sell");
    public static final String IECommand = "ItemEconomy";
    public static final String TaxCommand = "Tax";

    //currency, only diamond for now
    public static final Material currency = Material.DIAMOND;
    public static final Material currency_block = Material.DIAMOND_BLOCK;

    //smartshop finals
    public static final List<String> SS_subCommands = List.of("info", "accept", "decline", "remove", "generate", "log", "save", "reload", "search");
    public static final List<String> SS_aliases = List.of("smartShop", "ss", "s", "SmartShop");
    public static final String SS_MainCommand = "SmartShop";

    //unused
    public static final double michaelFactor = 0.50;
    public static final String michaelName = "SingleGear124";

    /**  MODIFIABLE VARIABLE NAMES*/
    //add to this list when creating new config variables, or else load config check will fail
    public static final List<String> IEConfigNames = List.of("enableTaxe","enableSmartShop","defaultDebug","maxAllowedInitializeAttempts","initializeTaskDelay");
    public static final List<String> TaxesConfigNames = List.of("nextTaxHours","taxCap","maxProfitTax","minimumProfit","wealthCap"," taxReturnCap","mainTaxDepositID");
    public static final List<String> SSConfigNames = List.of(" minP","maxP","minQ","maxQ","earningFactor","nextOrderHours","smartShopHolderName","shopDepositCost","taxAccountAsBuyer","maxTakeFromTaxAccountPercentage");

    /**  DEFAULT VALUES */
    //add default variables here, will default to these if load config check fails
    //main
    public static boolean enableTaxes = true;
    public static boolean enableSmartShop = true;
    public static boolean defaultDebug = true;
    public static boolean enableJobsIntegration = true;
    public static int maxAllowedInitializeAttempts = 5;
    public static int initializeTaskDelay = 200;

    //taxes
    public static int nextTaxHours = 24;
    public static double taxCap = 50.0;
    public static double maxProfitTax = 20.0;
    public static int minimumProfit = 5;
    public static double wealthCap = 70.0;
    public static double taxReturnCap = 20.0;
    public static String mainTaxDepositID = "Tax";

    //smartShop
    public static int minP = 70;
    public static int maxP = 110;
    public static int minQ = 5;
    public static int maxQ = 80;
    public static double earningFactor = 0.20;
    public static int nextOrderHours = 24;
    public static String smartShopHolderName = "CHINA";
    public static int shopDepositCost = 10;
    public static boolean taxAccountAsBuyer = true;
    public static double maxTakeFromTaxAccountPercentage = 0.10;

    /** Create and Load config file functions*/
    public static void loadConfig() throws IOException {

        InputStream inputStream = DataManager.getDataStreamYML(configFileName);
        Yaml yaml = new Yaml();
        ItemEconomyConfig = yaml.load(inputStream);
        boolean flag = true;

        if(ItemEconomyConfig != null){
            for (String key:IEConfigNames)
                if(!ItemEconomyConfig.containsKey(key))
                    flag = false;

            if(ItemEconomyConfig.containsKey("Tax Settings")) {
                TaxesConfig = (Map<String, Object>) ItemEconomyConfig.get("Tax Settings");

                for (String key : TaxesConfigNames)
                    if (!TaxesConfig.containsKey(key))
                        flag = false;
            } else{
                flag = false;
            }

            if(ItemEconomyConfig.containsKey("SmartShop Settings")) {
                SmartShopConfig = (Map<String, Object>) ItemEconomyConfig.get("SmartShop Settings");

                for (String key : SSConfigNames)
                    if (!SmartShopConfig.containsKey(key))
                        flag = false;
            } else {
                flag = false;
            }
        } else{
            flag = false;
        }

        if(!flag){
            createConfig();
        }

    }

//    /** Create and Load config file functions*/
//    public static void loadConfig() throws FileNotFoundException {
//
//        InputStream inputStream = DataManager.getDataStreamYML(configFileName);
//        Yaml yaml = new Yaml();
//        Map<String, Object> data = yaml.load(inputStream);
//
//        if(data != null){
//            if(data.containsKey("enableTaxes"))
//                enableTaxes = (boolean)data.get("enableTaxes");
//            if(data.containsKey("enableSmartShop"))
//                enableSmartShop = (boolean)data.get("enableSmartShop");
//            if(data.containsKey("maxAllowedInitializeAttempts"))
//                maxAllowedInitializeAttempts = (int)data.get("maxAllowedInitializeAttempts");
//            if(data.containsKey("initializeTaskDelay"))
//                initializeTaskDelay = (int)data.get("initializeTaskDelay");
//            if(data.containsKey("defaultDebug"))
//                defaultDebug = (boolean)data.get("defaultDebug");
//
//            if(data.containsKey("Tax Settings")){
//                Map<String,Object> taxSettings = (Map<String, Object>) data.get("Tax Settings");
//                if(taxSettings != null){
//                    if(taxSettings.containsKey("nextTaxHours"))
//                        nextTaxHours = (int)taxSettings.get("nextTaxHours");
//                    if(taxSettings.containsKey("taxCap"))
//                        taxCap = (double)taxSettings.get("taxCap");
//                    if(taxSettings.containsKey("maxProfitTax"))
//                        maxProfitTax = (double)taxSettings.get("maxProfitTax");
//                    if(taxSettings.containsKey("minimumProfit"))
//                        minimumProfit = (int)taxSettings.get("minimumProfit");
//                    if(taxSettings.containsKey("wealthCap"))
//                        wealthCap = (double)taxSettings.get("wealthCap");
//                    if(taxSettings.containsKey("taxReturnCap"))
//                        taxReturnCap = (double)taxSettings.get("taxReturnCap");
//                    if(taxSettings.containsKey("mainTaxDepositID"))
//                        mainTaxDepositID = (String)taxSettings.get("mainTaxDepositID");
//                }
//            }
//
//            if(data.containsKey("SmartShop Settings")){
//                Map<String,Object> smartShopSettings = (Map<String, Object>) data.get("SmartShop Settings");
//                if(smartShopSettings != null){
//                    if(smartShopSettings.containsKey("minP"))
//                        minP = (int)smartShopSettings.get("minP");
//                    if(smartShopSettings.containsKey("maxP"))
//                        maxP = (int)smartShopSettings.get("maxP");
//                    if(smartShopSettings.containsKey("minQ"))
//                        minQ = (int)smartShopSettings.get("minQ");
//                    if(smartShopSettings.containsKey("maxQ"))
//                        maxQ = (int)smartShopSettings.get("maxQ");
//                    if(smartShopSettings.containsKey("earningFactor"))
//                        earningFactor = (double)smartShopSettings.get("earningFactor");
//                    if(smartShopSettings.containsKey("nextOrderHours"))
//                        nextOrderHours = (int)smartShopSettings.get("nextOrderHours");
//                    if(smartShopSettings.containsKey("smartShopHolderName"))
//                        smartShopHolderName = (String)smartShopSettings.get("smartShopHolderName");
//                    if(smartShopSettings.containsKey("shopDepositCost"))
//                        shopDepositCost = (int)smartShopSettings.get("shopDepositCost");
//                    if(smartShopSettings.containsKey("taxAccountAsBuyer"))
//                        taxAccountAsBuyer = (boolean)smartShopSettings.get("taxAccountAsBuyer");
//                    if(smartShopSettings.containsKey("maxTakeFromTaxAccountPercentage"))
//                        maxTakeFromTaxAccountPercentage = (double)smartShopSettings.get("maxTakeFromTaxAccountPercentage");
//                }
//            }
//        }
//    }


    //creates .yml config file from current static variables
    public static void createConfig() throws IOException {
        File configFile = DataManager.createDataFileYML(configFileName);

        Map<String,Object> dataMap = new HashMap<>();
        //populate map
        dataMap.put("enableTaxes",enableTaxes);
        dataMap.put("enableSmartShop",enableSmartShop);
        dataMap.put("maxAllowedInitializeAttempts",maxAllowedInitializeAttempts);
        dataMap.put("initializeTaskDelay",initializeTaskDelay);
        dataMap.put("defaultDebug",defaultDebug);
        dataMap.put("enableJobsIntegration",enableJobsIntegration);

        Map<String,Object> taxDataMap = new HashMap<>();
        taxDataMap.put("nextTaxHours",nextTaxHours);
        taxDataMap.put("maxProfitTax",maxProfitTax);
        taxDataMap.put("taxReturnCap",taxReturnCap);
        taxDataMap.put("wealthCap",wealthCap);
        taxDataMap.put("mainTaxDepositID",mainTaxDepositID );

        Map<String,Object> smartShopDataMap = new HashMap<>();
        smartShopDataMap.put("minP",minP);
        smartShopDataMap.put("maxP",maxP);
        smartShopDataMap.put("minQ",minQ);
        smartShopDataMap.put("maxQ",maxQ);
        smartShopDataMap.put("earningFactor",earningFactor);
        smartShopDataMap.put("nextOrderHours",nextOrderHours);
        smartShopDataMap.put("smartShopHolderName",smartShopHolderName);
        smartShopDataMap.put("shopDepositCost",shopDepositCost);
        smartShopDataMap.put("taxAccountAsBuyer",taxAccountAsBuyer);
        smartShopDataMap.put("maxTakeFromTaxAccountPercentage",maxTakeFromTaxAccountPercentage);

        dataMap.put("Tax Settings",taxDataMap);
        dataMap.put("SmartShop Settings",smartShopDataMap);

        PrintWriter writer = new PrintWriter(configFile);
        Yaml yaml = new Yaml();
        yaml.dump(dataMap, writer);
    }
}
