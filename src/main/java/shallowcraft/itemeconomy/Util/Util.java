package shallowcraft.itemeconomy.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Data.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Transaction.ResultType;
import shallowcraft.itemeconomy.Vault.Vault;
import shallowcraft.itemeconomy.Vault.VaultType;

import java.util.*;

public class Util {
    /**
     * Find a valid container block for a given sign, if it exists.
     *
     * @param sign sign to check
     * @return container for the sign if available, null otherwise.
     */
    //not my code
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

    public static boolean isVault(Block containerVault, Map<String, Account> accounts) {
        for (Account acc : accounts.values()) {
            for (Vault vault : acc.getVaults()) {
                if (vault.getContainer().getLocation().equals(containerVault.getLocation())) {
                    return true;

                }

            }
        }
        return false;
    }

    /**
     * Return whether the given material is a valid container type for item vaults.
     *
     * @param material material to check
     * @return whether the given material is a valid container type for item vaults
     */
    public static boolean isValidContainer(Material material) {
        return Config.VaultContainerTypes.contains(material);
    }


//    public static boolean isValidVaultSign(Sign sign) {
//        boolean isVaultSign = false;
//
//        ItemEconomy.log.info("CHECKING SIGN");
//
//        String dataString = sign.getPersistentDataContainer().get(new NamespacedKey(ItemEconomy.getInstance(), Config.PDCSignKey), PersistentDataType.STRING);
//        ItemEconomy.log.info("PDC DATA: " + dataString);
//
//        if (dataString != null)
//            isVaultSign = Boolean.parseBoolean(dataString);
//
//
//        return isVaultSign;
//    }

    //temp fix until i figure out meta data
    public static boolean isValidVaultSign(Sign sign) {
        if(sign.isPlaced()){
            Block container = Util.chestBlock(sign);
            if(container != null)
                return Util.isVault(container, ItemEconomy.getInstance().getAccounts());
        }

        return false;
    }


    public static boolean isValidVaultSignText(SignChangeEvent sign) {
        return sign.lines().contains(Component.text(Config.vaultHeader));
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

        if (toRemove <= inStack)
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
    public static ItemStack convertToItem(int amount, ItemStack blockStack, Inventory inventory) {
        int slot = inventory.firstEmpty();

        if (slot != -1) {
            blockStack.setAmount(blockStack.getAmount() - amount);
            inventory.setItem(slot, new ItemStack(Config.currency, amount * 9));
            //ItemEconomy.log.info("conversion of 1 " + amount + "blocks to items result: " + (inventory.getItem(slot) != null));
            return inventory.getItem(slot);
        }

        return null;
    }

    //amount must be divisible by 9
    public static ItemStack convertToBlock(int amount, ItemStack itemStack, Inventory inventory) {
        int slot = inventory.firstEmpty();

        if (slot != -1) {
            itemStack.setAmount(itemStack.getAmount() - amount);
            inventory.setItem(slot, new ItemStack(Config.currency_block, amount / 9));
            return inventory.getItem(slot);
        }

        return null;
    }


    public static EconomyResponse.ResponseType convertResponse(ResultType resultType) {
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

    public static VaultType getVaultType(String vaultType) {
        if (vaultType == null)
            return VaultType.REGULAR;

        if (vaultType.isEmpty())
            return VaultType.REGULAR;


        switch (vaultType) {
            case "[Withdraw]":
                return VaultType.WITHDRAW_ONLY;
            case "[Deposit]":
                return VaultType.DEPOSIT_ONLY;
            default:
                return VaultType.REGULAR;
        }
    }

    //not my code
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static int getAllVaultsBalance(List<Vault> vaults) {
        int count = 0;
        for (Vault vault : new ArrayList<>(vaults)) {
            int current = vault.getVaultBalance();
            if (current > 0)
                count += current;
        }
        return count;
    }

    public static List<String> getAllPlayerNames() {
        List<String> output = new ArrayList<>();
        for (OfflinePlayer p : ItemEconomy.getInstance().getServer().getOfflinePlayers()) {
            output.add(p.getName());
        }

        return output;
    }

    public static List<String> getAllPlayerIDs() {
        List<String> output = new ArrayList<>();
        for (OfflinePlayer p : ItemEconomy.getInstance().getServer().getOfflinePlayers()) {
            output.add(p.getUniqueId().toString());
        }

        return output;
    }


    public static Vault getVaultFromSign(Sign sign, Map<String, Account> accounts) {
        for (Account acc : accounts.values()) {
            for (Vault vault : acc.getVaults()) {
                if (vault.getSign().getLocation().equals(sign.getLocation()))
                    return vault;
            }
        }

        return null;
    }

    public static Inventory getInventory(OfflinePlayer player){
        Inventory inv = null;

        try{
            inv = player.getPlayer().getInventory();
        } catch (Exception ignored){
        }

        return inv;
    }

    public static int getTotalCirculation(){
        int totalCirculation = 0;
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();

        for (Account acc:accounts.values()) {
            if(acc instanceof PlayerAccount)
                totalCirculation += acc.getBalance();
        }

        return totalCirculation;
    }

    public static boolean isPlayerName(String name){
        return Util.getAllPlayerNames().contains(name);
    }

    public static boolean isPlayerID(String id){
        return Util.getAllPlayerIDs().contains(id);
    }

    public static String getPlayerID(String name){
        String id = null;

        try {
            id = ItemEconomy.getInstance().getServer().getPlayerUniqueId(name).toString();
        } catch (Exception ignored){
        }

        return id;
    }

}
