package shallowcraft.itemeconomy.Data;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;

public class Config {
    public static final String dataFileName = "economyData";
    public static final String taxID = "Tax";
    public static final String shopID = "Shop";
    public static final Material currency = Material.DIAMOND;
    public static final Material currency_block = Material.DIAMOND_BLOCK;
    public static final String vaultHeader = "[Vault]";
    public static final List<String> IESubCommands = List.of("create_account",
            "balance", "list_accounts", "create_account_all", "remove_account",
            "reload", "save", "baltop", "deposit", "withdraw");
    public static final List<String> IECommandAliases = List.of("ItemEconomy", "itemeconomy",
            "IE", "ie", "eco", "money");
    public static final List<String> IEShopCommandAliases = List.of("Shop","ItemEconomyShop", "itemeconomyshop", "ieshop" , "shop", "ss", "buy", "sell");
    public static final String IECommand = "ItemEconomy";
    public static final String PDCSignKey = "IsVaultSign";
    public static HashSet<Material> VaultContainerTypes = new HashSet<>(List.of(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL));
}
