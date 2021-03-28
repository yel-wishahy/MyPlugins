package shallowcraft.itemeconomy;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

    public static boolean isValidVaultSign(Sign sign){
        String line = sign.line(0).toString();
        if(line == "[Vault]")
            return true;
        else
            return false;
    }

    public static boolean isBlockOfItem(Material item, Material block){
        String itemName = item.toString();
        String blockName = block.toString();
        if(blockName.contains("_"))
            blockName = blockName.split("_")[0];

        return itemName.equals(blockName);
    }

    public static boolean hasAccount(OfflinePlayer player, List<Account> accounts){
        for (Account acc:accounts) {
            if(acc.getPlayer().getUniqueId().equals(player.getUniqueId()))
                return true;
        }

        return false;
    }

    public static Account getAccount(OfflinePlayer player, List<Account> accounts){
        for (Account acc:accounts) {
            if (acc.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return acc;
            }
        }

        return null;
    }

    public static int countItem(Inventory inventory, Material itemType ){
        int itemCount = 0;
        for (ItemStack stack:inventory.getContents()) {
            if(stack != null) {
                if (stack.getData().getItemType().equals(itemType))
                    itemCount += stack.getAmount();

                if(Util.isBlockOfItem(itemType, stack.getType())){
                    itemCount+=stack.getAmount() * 9;
                }
            }
        }

        return itemCount;
    }

}
