package EXPTrade;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.*;
import org.bukkit.util.Vector;
import java.util.*;

public class XPTrade extends JavaPlugin implements Listener {

    private List<TradeSign> tradeSigns;

    @Override
    public void onEnable() {
        //Fired when the server enables the plugin
        Bukkit.getPluginManager().registerEvents(this, this);
        tradeSigns = new ArrayList<TradeSign>();
    }

    @Override
    public void onDisable() {
        //Fired when the server stops and disables all plugins
    }

    @EventHandler
    public void onCreateTradeSign(SignChangeEvent signEvent) {
        Player player = signEvent.getPlayer();
        player.sendMessage("creating sign trade");
        player.sendMessage(signEvent.getLines());
        if (Objects.equals(signEvent.getLine(0), "[xpTrade]")) {
            int levels = 0;
            char lChar = 'L';
            int numDiamonds = 0;
            String diamondID = "none";
            player.sendMessage("1");

            try {
                String[] xpProduct = Objects.requireNonNull(signEvent.getLine(1)).split(" ");
                String[] diamondsCost = Objects.requireNonNull(signEvent.getLine(1)).split(" ");

                levels = Integer.parseInt(xpProduct[0]);
                lChar = xpProduct[1].charAt(0);

                numDiamonds = Integer.parseInt(diamondsCost[0]);
                diamondID = diamondsCost[1];
                player.sendMessage("2");
            } catch (Exception e) {
                return;
            }

            if (levels > 0 && numDiamonds > 0 && diamondID.toLowerCase(Locale.ROOT).equals("diamond") && lChar == 'L') {
                Location chestLoc = getChestLocation((Sign) signEvent.getBlock());
                if(chestLoc != null) {
                    TradeSign tradeSign = new TradeSign(player.getUniqueId(), levels, numDiamonds, signEvent.getBlock().getLocation(), chestLoc);
                    tradeSigns.add(tradeSign);
                    player.sendMessage("sign trade created");
                }
            }
        }
    }

    @EventHandler
    public void onBuyXP(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasBlock()) {
            if (event.getClickedBlock() instanceof Sign) {
                player.sendMessage("buy attempt");
                TradeSign tradeSign = getTradeSign((Sign) event.getClickedBlock());

                if (tradeSign != null) {
//                    if (!tradeSign.getPlayerUUID().equals(player.getUniqueId()) && validateInventory(player.getInventory(), tradeSign.getDiamondCost())) {
                    if (validateInventory(player.getInventory(), tradeSign.getDiamondCost())) {
                        player.giveExp(getXPForLevel(tradeSign.getLevelProduct()));
                        player.getInventory().remove(new ItemStack(Material.DIAMOND, tradeSign.getDiamondCost()));
                        player.sendMessage(ChatColor.GREEN + "Nice, you have bought >> " + ChatColor.GOLD + tradeSign.getLevelProduct() + " L " + ChatColor.GREEN + "<< of xp for >>" + ChatColor.GOLD + tradeSign.getDiamondCost() + ChatColor.AQUA + " Diamonds!");
                        Chest chest = (Chest) tradeSign.getChestLoc().getBlock();
                        chest.getBlockInventory().addItem(new ItemStack(Material.DIAMOND, tradeSign.getDiamondCost()));
                    } else {
                        player.sendMessage(ChatColor.RED + "Damn, XP Transaction FAILED.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onModifyXP(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("attempt to add xp");
        if (event.hasBlock()) {
            if (event.getClickedBlock() instanceof Sign) {
                TradeSign tradeSign = getTradeSign((Sign) event.getClickedBlock());

                if (tradeSign != null) {
//                    if (!tradeSign.getPlayerUUID().equals(player.getUniqueId()) && validateInventory(player.getInventory(), tradeSign.getDiamondCost())) {
                    if (player.getLevel() > 0){
                        tradeSign.addXP(getXPForLevel(player.getLevel()));
                        player.giveExpLevels(-1);
                    }
                }
            }
        }
    }

    private TradeSign getTradeSign(Sign sign) {
        for (TradeSign tradeSign : tradeSigns) {
            if (tradeSign.getSignLoc().equals(sign.getLocation())) {
                return tradeSign;
            }
        }

        return null;
    }

    private boolean validateInventory(Inventory inventory, int numDiamonds) {
        int count = 0;

        for (ItemStack is : inventory.getContents()) {

            if (Objects.requireNonNull(is.getData()).getItemType().equals(Material.DIAMOND)) {
                count += is.getAmount();
            }

            if (count >= numDiamonds) {
                return true;
            }
        }

        return false;
    }

    private Location getChestLocation(Sign sign){
        int deltaZ = 0;
        int deltaX = 0;

        org.bukkit.material.Attachable signMaterial = (org.bukkit.material.Attachable) sign.getData();
        Block attachedBlock = sign.getBlock().getRelative(signMaterial.getAttachedFace());
        Chest chest = (org.bukkit.block.Chest) attachedBlock.getLocation().getBlock().getState();

        if(chest != null)
            return chest.getLocation();

        return null;
    }

    public static int getXPForLevel(int lvl) {
        if (lvl <= 15) {
            return lvl * 17;
        } else if (lvl > 16 && lvl < 31) {
            return (int) (1.5 * Math.pow(lvl, 2) - 29.5 * lvl + 360);
        } else if (lvl > 30) {
            return (int) (3.5 * Math.pow(lvl, 2) - 151.5 * lvl + 2220);
        }
        return 0;
    }

    public static int getLevelForXP(int xp) {
        if (xp <= 15 * 17) {
            return xp / 17;
        } else if (xp < (int) (1.5 * Math.pow(31, 2) - 29.5 * 15 + 360)) {
            return (int) (Math.sqrt((xp - 155) / 1.5) + 9.83333);
        } else {
            return (int) (Math.sqrt((xp - 580.55) / 3.5) + 21.64285);
        }
    }


}
