package shallowcraft.itemeconomy;

import org.bukkit.Material;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;

//should probably move this to json, but some things like command aliases etc shouldnt be configurable
public class Config {
    //enable
    public static final boolean enableTaxes = true;
    public static final boolean enableSmartShop = true;

    //modifiable
    public static int nextTaxHours = 24;
    public static double taxCap = 50.0;
    public static double maxProfitTax = 20.0;
    public static int minimumProfit = 5;
    public static double wealthCap = 70.0;
    public static double taxReturnCap = 20.0;

    //finals (dont change)
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
    public static final SimpleDateFormat shortTimeFormat = new SimpleDateFormat("MM.dd 'at' HH:mm");
    public static final String dataFileName = "economyData";
    public static final String taxID = "Tax";
    public static final String shopID = "Shop";
    public static final Material currency = Material.DIAMOND;
    public static final Material currency_block = Material.DIAMOND_BLOCK;
    public static final String vaultHeader = "[Vault]";
    public static final List<String> IESubCommands = List.of("create_account",
            "balance", "list_accounts", "create_account_all", "remove_account",
            "reload", "save","load", "baltop", "admindeposit", "adminwithdraw","withdraw",
            "transfer", "admintransfer", "statsupdate", "debug");
    public static final List<String> IECommandAliases = List.of("ItemEconomy", "itemeconomy",
            "IE", "ie", "eco", "money");
    public static final List<String> TaxCommandAliases = List.of("tax", "ietax", "ieTax", "taxes", "t");
    public static final List<String> TaxSubCommands = List.of("add","addcustom", "remove", "tax", "taxall", "info", "clear", "edit", "redistribute", "taxprofits", "resetprofits");
    public static final List<String> TaxEditSubCommands = List.of("timeset_now", "set_rate");
    public static final List<String> IEShopCommandAliases = List.of("Shop","ItemEconomyShop", "itemeconomyshop", "ieshop" , "shop", "ss", "buy", "sell");
    public static final String IECommand = "ItemEconomy";
    public static final String TaxCommand = "Tax";
    public static final String PDCSignKey = "IsVaultSign";
    public static HashSet<Material> VaultContainerTypes = new HashSet<>(List.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL));
    public static final List<String> transferTypes = List.of("Deposit-Vault", "Withdraw-Vault", "Regular-Vault");
    public static final List<String> accountTypes = List.of("Player Account", "General Account");
}
