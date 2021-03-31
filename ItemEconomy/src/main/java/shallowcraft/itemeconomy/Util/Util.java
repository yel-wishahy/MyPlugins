package shallowcraft.itemeconomy.Util;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Item;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Core.Account;
import shallowcraft.itemeconomy.Core.ItemEconomy;
import shallowcraft.itemeconomy.Core.TransactionResult;

import java.util.List;
import java.util.Objects;

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

        return sign != null && sign.isPlaced();
    }

    public static boolean isValidVaultSign(SignChangeEvent sign) {
        String header = ((TextComponent) Objects.requireNonNull(sign.line(0))).content();

        return header != null && header.equals(Config.vaultHeader);
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
        //ItemEconomy.log.info("Inventory: " + inStack + " ToRemove: " + toRemove);
        int result = 0;

        if(toRemove <= inStack)
            result = toRemove;
        else
            result = inStack;

        //ItemEconomy.log.info("Toremove result: " + result);
        return result;
    }

    public static int amountToAdd(int inStack, int toAdd) {
        //ItemEconomy.log.info("Inventory: " + inStack + " ToAdd: " + toAdd);
        int result = inStack + toAdd;

        if (result > 64)
            result = 64 - inStack;
        else
            result = toAdd;

        //ItemEconomy.log.info("ToAdd result: " + result);

        return result;
    }

    public static int[] currencyToCurrencyBlock(int amount) {
        int blocks = amount / 9;
        int items = amount - blocks * 9;
        return new int[]{items, blocks};
    }

    //cannot be more than 5 blocks
    public static ItemStack convertToItem(int amount, ItemStack blockStack, Inventory inventory){
        int slot = inventory.firstEmpty();

        if(slot != -1){
            blockStack.setAmount(blockStack.getAmount() - amount);
            inventory.setItem(slot, new ItemStack(Config.currency, amount * 9));
            //ItemEconomy.log.info("conversion of 1 " + amount + "blocks to items result: " + (inventory.getItem(slot) != null));
            return inventory.getItem(slot);
        }

        return null;
    }

    //amount must be divisible by 9
    public static ItemStack convertToBlock(int amount, ItemStack itemStack, Inventory inventory){
        int slot = inventory.firstEmpty();

        if(slot != -1){
            itemStack.setAmount(itemStack.getAmount() - amount);
            inventory.setItem(slot, new ItemStack(Config.currency_block, amount / 9));
            return inventory.getItem(slot);
        }

        return null;
    }


    public static EconomyResponse.ResponseType convertResponse(TransactionResult.ResultType resultType) {
        switch (resultType) {
            case FAILURE:
                return EconomyResponse.ResponseType.FAILURE;
            case SUCCESS:
                return EconomyResponse.ResponseType.SUCCESS;
            case NOT_IMPLEMENTED:
                return EconomyResponse.ResponseType.NOT_IMPLEMENTED;
            case INSUFFICIENT_FUNDS:
                return EconomyResponse.ResponseType.FAILURE;
            case INSUFFICIENT_SPACE:
                return EconomyResponse.ResponseType.FAILURE;
            default:
                return EconomyResponse.ResponseType.FAILURE;
        }
    }
}
