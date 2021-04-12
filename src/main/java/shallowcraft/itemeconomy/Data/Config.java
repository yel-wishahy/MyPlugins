package shallowcraft.itemeconomy.Data;

import org.bukkit.Material;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;

public class Config {
    public static final SimpleDateFormat taxTimeFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
    public static int nextTaxHours = 24;
    public static int taxCap = 50;
    public static final String dataFileName = "economyData";
    public static final double wealthCap = 0.70;
    public static final String taxID = "Tax";
    public static final String shopID = "Shop";
    public static final Material currency = Material.DIAMOND;
    public static final Material currency_block = Material.DIAMOND_BLOCK;
    public static final String vaultHeader = "[Vault]";
    public static final List<String> IESubCommands = List.of("create_account",
            "balance", "list_accounts", "create_account_all", "remove_account",
            "reload", "save", "baltop", "deposit", "withdraw", "redistribute");
    public static final List<String> IECommandAliases = List.of("ItemEconomy", "itemeconomy",
            "IE", "ie", "eco", "money");
    public static final List<String> TaxCommandAliases = List.of("tax", "ietax", "ieTax", "taxes", "t");
    public static final List<String> TaxSubCommands = List.of("add_tax", "remove_tax", "tax", "tax_all", "tax_info", "clear_tax");
    public static final List<String> IEShopCommandAliases = List.of("Shop","ItemEconomyShop", "itemeconomyshop", "ieshop" , "shop", "ss", "buy", "sell");
    public static final String IECommand = "ItemEconomy";
    public static final String TaxCommand = "Tax";
    public static final String PDCSignKey = "IsVaultSign";
    public static HashSet<Material> VaultContainerTypes = new HashSet<>(List.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL));
}
