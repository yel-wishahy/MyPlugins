package shallowcraft.shopfind;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.QuickShopAPI;
import org.maxgamer.quickshop.chat.QuickComponentImpl;
import org.maxgamer.quickshop.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ShopFind extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    public static List<Shop> shops = new ArrayList<>();

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().isPluginEnabled("QuickShop")) {
            updateShops();
        } else {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void updateShops(){
        shops = QuickShopAPI.getShopAPI().getAllShops();
    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return super.onCommand(sender, command, label, args);
    }
}
