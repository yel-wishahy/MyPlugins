package shallowcraft.itemeconomy.Util;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Core.Account;
import shallowcraft.itemeconomy.Core.TransactionResult;

import java.util.List;

public class Util {
    /**
     * Find a valid container block for a given sign, if it exists.
     *
     * @param sign sign to check
     * @return container for the sign if available, null otherwise.
     */
    public static Block chestBlock(Sign sign) {
        // is sign attached to a valid vault container?
        Block signBlock = sign.getBlock();
        BlockData blockData = signBlock.getBlockData();

        if (!(blockData instanceof WallSign)) {
            return null;
        }

        WallSign signData = (WallSign) blockData;
        BlockFace attached = signData.getFacing().getOppositeFace();

        // allow either the block sign is attached to or the block below the sign as chest block. Prefer attached block.
        Block blockAttached = signBlock.getRelative(attached);
        Block blockBelow = signBlock.getRelative(BlockFace.DOWN);

        return isValidContainer(blockAttached.getType()) ? blockAttached : isValidContainer(blockBelow.getType()) ? blockBelow : null;
    }

    /**
     * Return whether the given material is a valid container type for item vaults.
     *
     * @param material material to check
     * @return whether the given material is a valid container type for item vaults
     */
    public static boolean isValidContainer(Material material) {
        switch (material) {
            case CHEST:
            case TRAPPED_CHEST:
            case BARREL:
                return true;
            default:
                return false;
        }
    }

    public static boolean isValidVaultSign(Sign sign) {
        return sign.isPlaced();
        //return sign.isPlaced() && sign.line(0).toString().contains("[Vault]");
    }

    public static boolean hasAccount(OfflinePlayer player, List<Account> accounts) {
        for (Account acc : accounts) {
            if (acc.getPlayer().getUniqueId().equals(player.getUniqueId()))
                return true;
        }

        return false;
    }

    public static Account getAccount(OfflinePlayer player, List<Account> accounts) {
        for (Account acc : accounts) {
            if (acc.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return acc;
            }
        }

        return null;
    }

    public static int countItem(Inventory inventory) {
        int itemCount = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack != null) {
                if (stack.getType().equals(Config.currency))
                    itemCount += stack.getAmount();

                if (stack.getType().equals(Config.currency_block)) {
                    itemCount += stack.getAmount() * 9;
                }
            }
        }

        return itemCount;
    }

    public static int amountToRemove(int inStack, int toRemove) {
        int result = inStack - toRemove;

        if (result < 0)
            return toRemove + result;

        if (result > 0)
            return toRemove - result;

        return toRemove;
    }

    public static int amountToAdd(int toAdd){
        return Math.min(toAdd, 64);
    }

    public static int[] currencyToCurrencyBlock(int amount) {
        int blocks = amount / 9;
        int items = amount - blocks * 9;
        return new int[]{items, blocks};
    }

    public static TransactionResult withdraw(Inventory inventory, int amount) {
        int numRemoved = 0;

        for (ItemStack stack : inventory) {
            if (numRemoved >= amount)
                break;

            if (stack.getType().equals(Config.currency)) {
                int toRemove = Util.amountToRemove(stack.getAmount(), amount);
                stack.setAmount(stack.getAmount() - toRemove);
                numRemoved += toRemove;
            }

            if (stack.getType().equals(Config.currency_block)) {
                int toRemove = Util.amountToRemove(stack.getAmount() * 9, amount);
                int[] result = Util.currencyToCurrencyBlock(toRemove);
                int items = result[0];
                int blocks = result[1];

                int slot = inventory.firstEmpty();
                if (slot != -1) {
                    inventory.setItem(slot, new ItemStack(Config.currency, items));
                } else {
                    return new TransactionResult(numRemoved, TransactionResult.ResultType.INSUFFICIENT_SPACE, "withdraw");
                }

                stack.setAmount(stack.getAmount() - blocks);
                numRemoved += items + blocks * 9;
            }
        }

        return new TransactionResult(numRemoved, TransactionResult.ResultType.SUCCESS, "withdraw");
    }

    public static TransactionResult deposit(Inventory inventory, int amount){
        int numAdded = 0;

        while(numAdded <= amount){
            int slot = inventory.firstEmpty();

            if(slot == -1 && numAdded <= amount)
                return new TransactionResult(numAdded, TransactionResult.ResultType.INSUFFICIENT_SPACE, "deposit");

            int toAdd = amountToAdd(amount);
            inventory.setItem(slot, new ItemStack(Config.currency, toAdd));
            numAdded+=toAdd;
        }

        return new TransactionResult(numAdded, TransactionResult.ResultType.SUCCESS, "deposit");
    }

    public static EconomyResponse.ResponseType convertResponse(TransactionResult.ResultType resultType){
        switch (resultType){
            case FAILURE:
                return  EconomyResponse.ResponseType.FAILURE;
            case SUCCESS:
                return  EconomyResponse.ResponseType.SUCCESS;
            case NOT_IMPLEMENTED:
                return  EconomyResponse.ResponseType.NOT_IMPLEMENTED;
            case INSUFFICIENT_FUNDS:
                return EconomyResponse.ResponseType.FAILURE;
            case INSUFFICIENT_SPACE:
                return EconomyResponse.ResponseType.FAILURE;
            default:
                return EconomyResponse.ResponseType.FAILURE;
        }
    }
}
