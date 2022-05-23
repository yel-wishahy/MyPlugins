package shallowcraft.smartshop.Commads;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import shallowcraft.itemeconomy.Permissions;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.smartshop.ShopOrder.ShopOrder;
import shallowcraft.smartshop.ShopOrder.ShopOrderLog;
import shallowcraft.smartshop.SmartShop;
import shallowcraft.smartshop.SmartShopUtil;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmartShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        Player player = null;
        Map<String, List<ShopOrder>> shopOrders = SmartShop.getInstance().getShopOrders();

        if (commandSender instanceof Player)
            player = (Player) commandSender;

        if (args.length < 1)
            return false;

        switch (args[0]){
            case "search":
                if(args.length == 2 && commandSender instanceof Player)
                    commandSender.sendMessage(SmartShopUtil.getSearchResultsMessage(player.getLocation(), args[1]));
                else {
                    commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Invalid command");
                }

                return true;
            case "info":
                if(args.length == 1 && player != null){
                    commandSender.sendMessage(SmartShopUtil.getOrdersInfoMessage(player.getUniqueId().toString()));
                } else if (args.length == 2){
                    String id = Util.getPlayerID(args[1]);
                    commandSender.sendMessage(SmartShopUtil.getOrdersInfoMessage(id));
                }
                return true;
            case "accept":
                if(args.length == 2 && args[1].equals("all")){
                    for (ShopOrder order:shopOrders.get(Util.getPlayerID(commandSender.getName()))) {
                        ShopOrder.ShopOrderResult r = order.executeShopOrder();
                        commandSender.sendMessage(r.toString());
                    }
                } else if (args.length == 2){
                   ShopOrder s = SmartShopUtil.getOrderFromSummary(shopOrders.get(Util.getPlayerID(commandSender.getName())), args[1]);
                   if(s != null){
                       ShopOrder.ShopOrderResult r = s.executeShopOrder();
                       commandSender.sendMessage(r.toString());
                   }
                }
                SmartShop.getInstance().saveData();
                return true;
            case "decline":
                if(args.length == 2 && args[1].equals("all")){
                    String id = Util.getPlayerID(commandSender.getName());
                    SmartShopUtil.declineAllOrders(id);
                    commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Declined All Order");
                } else if (args.length == 2){
                    ShopOrder s = SmartShopUtil.getOrderFromSummary(shopOrders.get(Util.getPlayerID(commandSender.getName())), args[1]);
                    if(s != null){
                        SmartShopUtil.declineOrder(s);
                        commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Declined Order");
                    }
                }
                SmartShop.getInstance().saveData();
                return true;
            case "generate":
                if(commandSender.hasPermission(Permissions.adminPerm)){
                    if(args.length == 1){
                        TextComponent info = Component.text(ChatColor.GOLD + "[Smart Shop] " + ChatColor.GREEN
                                + "Generating shop orders, check your orders with the command: " + ChatColor.YELLOW + "s info ");
                        ItemEconomyPlugin.getInstance().getServer().broadcast(info, Permissions.msgPerm);
                        SmartShop.getInstance().generateOrderAll();
                    } else if(args.length ==2 && Util.isPlayerName(args[1]) ){
                        String id = Util.getPlayerID(args[1]);
                        SmartShop.getInstance().generateOrderForID(id);
                        commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.YELLOW + ChatColor.BOLD + "Generated single order!");
                    } else if (args.length == 2){
                        int amount = Integer.parseInt(args[1]);
                        SmartShop.getInstance().generateMultipleOrdersAll(amount);
                        TextComponent info = Component.text(ChatColor.GOLD + "[Smart Shop] " + ChatColor.GREEN
                                + "Generating shop orders, check your orders with the command: " + ChatColor.YELLOW + "s info ");
                        ItemEconomyPlugin.getInstance().getServer().broadcast(info, Permissions.msgPerm);
                    } else if (args.length == 3){
                        String id = Util.getPlayerID(args[1]);
                        int amount = Integer.parseInt(args[2]);
                        SmartShop.getInstance().generateMultipleOrdersForID(id, amount);
                        commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.YELLOW + ChatColor.BOLD + "Generated single orders!");
                    }
                    return true;
                } else {
                    commandSender.sendMessage(Permissions.invalidPerm);
                }
            case "remove":
                if(args.length == 3 && args[2].equals("all")){
                    String id = Util.getPlayerID(args[1]);
                    SmartShopUtil.declineAllOrders(id);
                    commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Removed All Order for " + ChatColor.YELLOW + args[1]);
                } else if (args.length == 3){
                    ShopOrder s = SmartShopUtil.getOrderFromSummary(shopOrders.get(Util.getPlayerID(args[1])), args[2]);
                    if(s != null){
                        SmartShopUtil.declineOrder(s);
                        commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Removed Order for " + ChatColor.YELLOW + args[1]);
                    }
                } else if(args.length == 2 && args[1].equals("all")){
                    for (String id:new ArrayList<>(SmartShop.getInstance().getShopOrders().keySet())) {
                        SmartShopUtil.declineAllOrders(id);
                    }
                    commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Removed All Orders");
                }
                SmartShop.getInstance().saveData();
                return true;
            case "log":
                if(args.length == 2 && commandSender instanceof Player){
                    String id = Util.getPlayerID(args[1]);
                    ItemStack book = ShopOrderLog.getInstance().getLogBook(id);

                    if(book != null){
                        int slot =  player.getInventory().firstEmpty();
                        if(slot != -1){
                            player.getInventory().setItem(slot, book);
                            commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.GREEN + "Log book for "
                                    + ChatColor.YELLOW + args[1] + ChatColor.GREEN + " has been sent to you.");
                        } else
                            commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Inventory is full, cannot give logbook");
                    } else {
                        commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "This player does not have any logs");
                    }
                } else {
                    commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.RED + ChatColor.BOLD + "Command Invalid");
                }
            case "save":
                SmartShop.getInstance().saveData();
                commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.YELLOW + ChatColor.BOLD + "Saved Data!");
                return true;
            case "reload":
                SmartShop.getInstance().loadData();
                commandSender.sendMessage(ChatColor.GOLD + "[Smart Shop] " + ChatColor.YELLOW + ChatColor.BOLD + "Reloaded Data!");
                return true;
            default:
                return false;
        }

    }
}
