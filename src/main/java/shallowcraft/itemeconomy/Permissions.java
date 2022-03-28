package shallowcraft.itemeconomy;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public class Permissions {
    public static final String adminPerm = "itemeconomy.admin";
    public static final String msgPerm = "itemeconomy.message";
    public static final String playerPerm = "itemeconomy.player";
    public static final String remoteWithdraw = "itemeconomy.withdraw";
    public static final String remoteDeposit = "itemeconomy.deposit";

    public static final String[] permissionsList = {adminPerm,msgPerm,playerPerm,remoteWithdraw,remoteDeposit};

    public static final String invalidPerm = ChatColor.GOLD + "[ItemEconomy] " + ChatColor.RED + "You do not have permissions to send this command.";
}
