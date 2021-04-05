package shallowcraft.itemeconomy;

import org.bukkit.Material;

import java.util.List;

public class Config {
    public static final String dataFileName = "economyData";
    public static final String taxID = "Tax";
    public static final String shopID = "Shop";
    public static final Material currency = Material.DIAMOND;
    public static final Material currency_block = Material.DIAMOND_BLOCK;
    public static final String vaultHeader = "[Vault]";
    public static final List<String> IECommands = List.of("create_account",
            "balance", "list_accounts", "create_account_all", "remove_account",
            "reload", "save", "baltop", "deposit", "withdraw");
    public static final List<String> IECommandAliases = List.of("ItemEconomy", "itemeconomy",
            "IE", "ie", "eco", "money");
    public static final List<String> IEShopCommandAliases = List.of("Shop","ItemEconomyShop", "itemeconomyshop", "ieshop" , "shop", "ss", "buy", "sell");
}
