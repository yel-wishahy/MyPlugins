package shallowcraft.itemeconomy.Util;

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Tax.taxable.Taxable;
import shallowcraft.itemeconomy.Tax.Taxation;
import shallowcraft.itemeconomy.Transaction.TransactionUtils;
import shallowcraft.itemeconomy.Transaction.Transaction;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;

import java.text.DecimalFormat;
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

    public static boolean isVault(Block containerVault) {
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
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
        return shallowcraft.itemeconomy.Config.VaultContainerTypes.contains(material);
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
        if (sign.isPlaced()) {
            Block container = Util.chestBlock(sign);
            if (container != null)
                return Util.isVault(container);
        }

        return false;
    }

    public static Vault getVaultFromContainer(Block block) {
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            for (Vault vault : acc.getVaults()) {
                if (vault.getContainer().getLocation().equals(block.getLocation())) {
                    return vault;
                }

            }
        }
        return null;

    }


    public static boolean isValidVaultSignText(SignChangeEvent sign) {
        return sign.lines().contains(Component.text(shallowcraft.itemeconomy.Config.vaultHeader));
    }

    public static int countItem(Inventory inventory) {
        int itemCount = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack != null) {
                if (stack.getType().equals(shallowcraft.itemeconomy.Config.currency))
                    itemCount += stack.getAmount();

                if (stack.getType().equals(shallowcraft.itemeconomy.Config.currency_block)) {
                    itemCount += stack.getAmount() * 9;
                }
            }
        }

        return itemCount;
    }

    public static boolean isAdmin(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        } else return sender.hasPermission("itemeconomy.admin");
    }

    public static boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
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
            inventory.setItem(slot, new ItemStack(shallowcraft.itemeconomy.Config.currency, amount * 9));
            //ItemEconomy.log.info("conversion of 1 " + amount + "blocks to items result: " + (inventory.getItem(slot) != null));
            return inventory.getItem(slot);
        }

        return null;
    }

    public static int getSlotToConvertToItem(int amount, ItemStack blockStack, int blockSlot, Inventory inventory) {
        int slot = inventory.firstEmpty();

        if (slot != -1) {
            blockStack.setAmount(blockStack.getAmount() - amount);
            inventory.setItem(blockSlot, blockStack);
            //ItemEconomy.log.info("conversion of 1 " + amount + "blocks to items result: " + (inventory.getItem(slot) != null));
            return slot;
        }

        return -1;
    }

    //amount must be divisible by 9
    public static ItemStack convertToBlock(int amount, ItemStack itemStack, Inventory inventory) {
        int slot = inventory.firstEmpty();

        if (slot != -1) {
            itemStack.setAmount(itemStack.getAmount() - amount);
            inventory.setItem(slot, new ItemStack(shallowcraft.itemeconomy.Config.currency_block, amount / 9));
            return inventory.getItem(slot);
        }

        return null;
    }


    public static EconomyResponse.ResponseType convertResponse(Transaction.ResultType resultType) {
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
        for (OfflinePlayer p : ItemEconomyPlugin.getInstance().getServer().getOfflinePlayers()) {
            output.add(p.getName());
        }

        return output;
    }

    public static List<String> getAllPlayerIDs() {
        List<String> output = new ArrayList<>();
        for (OfflinePlayer p : ItemEconomyPlugin.getInstance().getServer().getOfflinePlayers()) {
            output.add(p.getUniqueId().toString());
        }

        return output;
    }


    public static Vault getVaultFromSign(Sign sign) {
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            for (Vault vault : acc.getVaults()) {
                if (vault.getSign().getLocation().equals(sign.getLocation()))
                    return vault;
            }
        }

        return null;
    }

    public static Inventory getInventory(OfflinePlayer player) {
        Inventory inv = null;

        try {
            inv = player.getPlayer().getInventory();
        } catch (Exception ignored) {
        }

        return inv;
    }

    public static int getTotalCirculation() {
        int totalCirculation = 0;
        Map<String, Account> accounts = ItemEconomy.getInstance().getAccounts();

        for (Account acc : accounts.values()) {
            if (acc != null && !acc.getID().equals((String)Config.SmartShopConfig.get("smartShopHolderName")))
                totalCirculation += acc.getBalance(VaultType.ALL);
        }

        return totalCirculation;
    }

    public static boolean isPlayerName(String name) {
        return Util.getAllPlayerNames().contains(name);
    }

    public static boolean isPlayerID(String id) {
        return Util.getAllPlayerIDs().contains(id);
    }

    public static String getPlayerID(String name) {
        String id = null;

        try {
            id = ItemEconomyPlugin.getInstance().getServer().getPlayerUniqueId(name).toString();
        } catch (Exception ignored) {
        }

        return id;
    }

    public static List<String> getAllGeneralAccountIDs() {
        List<String> output = new ArrayList<>();

        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof GeneralAccount)
                output.add(acc.getID());
        }

        return output;
    }

    public static double totalTaxRate(PlayerAccount account) {
        double sum = 0;
        for (Taxable tax : account.getTaxes().values()) {
            sum += tax.getTaxRate();
        }

        return sum;
    }

    public static List<PlayerAccount> getPlayerAccounts() {
        List<PlayerAccount> output = new ArrayList<>();

        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof PlayerAccount)
                output.add((PlayerAccount) acc);
        }

        return output;
    }

    public static Map<String, Integer> getProfits() {
        Map<String, Integer> output = new HashMap<>();

        for (PlayerAccount acc : getPlayerAccounts()) {
            if (acc != null) {
                output.put(acc.getID(), acc.getProfit());
            }

        }

        return output;
    }

    public static void updateAllPlayerSavings() {
        for (PlayerAccount acc : getPlayerAccounts()) {
            if (acc != null) {
                acc.updateSavings();
            }
        }
    }

    public static List<Vault> getVaultsOfType(VaultType vaultType, List<Vault> vaults) {
        List<Vault> output = new ArrayList<>();

        for (Vault v : vaults) {
            if (v != null && v.getVaultType() == vaultType)
                output.add(v);
        }

        return output;
    }

    public static List<Vault> getVaultsOfNotType(VaultType vaultType, List<Vault> vaults) {
        List<Vault> output = new ArrayList<>();

        for (Vault v : vaults) {
            if (v != null && v.getVaultType() != vaultType)
                output.add(v);
        }

        return output;
    }

    public static VaultType getVaultTypeFromArgs(String arg) {
        switch (arg) {
            case "Deposit-Vault":
                return VaultType.DEPOSIT_ONLY;
            case "Withdraw-Vault":
                return VaultType.WITHDRAW_ONLY;
            default:
                return VaultType.REGULAR;
        }
    }

    public static String getPercentageBalanceChangeMessage(PlayerAccount holder) {
        double upBy = 0.0;
        if (holder.getLastStatsBalance() != 0)
            upBy = holder.getProfit() / ((double) holder.getLastStatsBalance()) * 100;
        String percentage = (new DecimalFormat("#.##")).format(upBy);
        StringBuilder s = new StringBuilder();
        s.append(ChatColor.GOLD).append(" ( ");

        if (upBy == 0)
            s.append(ChatColor.YELLOW).append(percentage).append(" %");
        else if (upBy > 0)
            s.append(ChatColor.GREEN).append("↑ ").append(percentage).append(ChatColor.YELLOW).append(" %");
        else if (upBy < 0)
            s.append(ChatColor.RED).append("↓ ").append(percentage).append(ChatColor.YELLOW).append(" %");

        s.append(ChatColor.GOLD).append(" ) ");

        return s.toString();
    }

    public static String getTaxInfo(PlayerAccount holder) {
        StringBuilder msg = new StringBuilder();

        msg.append(ChatColor.GOLD + "[ItemEconomy] " + ChatColor.AQUA + "Tax Information for: " + ChatColor.YELLOW + holder.getName() + " \n");
        int taxable = 0;

        try {
            taxable = Taxation.getInstance().getTaxableProfits().get(holder.getID());
        } catch (Exception ignored) {
        }

        msg.append(ChatColor.GREEN + " Calculated Income: " + ChatColor.YELLOW + holder.getProfit() + getPercentageBalanceChangeMessage(holder) + " \n");
        msg.append(ChatColor.GREEN + " Estimated Income Tax: " + ChatColor.YELLOW + taxable).append(ChatColor.GOLD).append(" ( ").append(ChatColor.YELLOW).
                append(((double) taxable) / holder.getProfit() * 100).append(" %").append(ChatColor.GOLD).append(" ) \n");

        if (holder.getTaxes().isEmpty())
            msg.append(ChatColor.GRAY + " No General Taxes (yay) \n");
        else
            msg.append(ChatColor.GREEN + " General Taxes: \n");

        for (Taxable tax : holder.getTaxes().values()) {
            if (tax != null) {
                msg.append(ChatColor.GREEN + "* Tax Name: " + ChatColor.AQUA).append(tax.getTaxName()).append(ChatColor.GREEN).append(" Tax %: ").
                        append(ChatColor.YELLOW).append(tax.getTaxRate()).append(ChatColor.GREEN).append(" Next Tax Time: ").append(ChatColor.YELLOW).
                        append(shallowcraft.itemeconomy.Config.timeFormat.format(tax.getNextTaxTime())).append("\n");
            }

        }


        return msg.toString();
    }

    public static String getPlayerName(String id) {
        return ItemEconomy.getInstance().getAccounts().get(id).getName();
    }

    public static int getCirculationShare() {
        int playerHolders = 0;
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof PlayerAccount)
                playerHolders++;
        }

        return getTotalCirculation() / playerHolders;
    }

    public static int getPlayerAccountCount() {
        int playerHolders = 0;

        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof PlayerAccount)
                playerHolders++;
        }

        return playerHolders;
    }

    public static int getAveragePlayerBalance() {
        int count = 0;
        int total = 0;
        for (Account acc : ItemEconomy.getInstance().getAccounts().values())
            if (acc instanceof PlayerAccount) {
                total += acc.getBalance(VaultType.ALL);
                count++;
            }

        if (count == 0)
            return 0;

        return total / count;
    }

    public static int getMedianPlayerBalance() {
        Map<String, Integer> bals = new HashMap<>();
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof PlayerAccount)
                bals.put(acc.getName(), acc.getBalance(VaultType.ALL));
        }
        bals = Util.sortByValue(bals);
        int mid = bals.size() / 2;
        if (bals.size() % 2 != 0)
            mid = bals.size() / 2 + 1;

        return (new ArrayList<>(bals.values())).get(mid);
    }

    public static Map<String, Integer> getPlayerSpendings() {
        Map<String, Integer> output = new HashMap<>();
        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc instanceof PlayerAccount)
                output.put(acc.getID(), ((PlayerAccount) acc).getNetWithdraw());
        }

        return output;
    }

    public static int getMedianPlayerSpendings() {
        Map<String, Integer> spendings = sortByValue(getPlayerSpendings());

        int mid = spendings.size() / 2;
        if (spendings.size() % 2 != 0)
            mid = spendings.size() / 2 + 1;

        return (new ArrayList<>(spendings.values())).get(mid);
    }

    public static void updateServerStats() {
        Map<String, String> newStats = new HashMap<>();
        newStats.put("Circulation", String.valueOf(getTotalCirculation()));
        newStats.put("Average Balance", String.valueOf(getAveragePlayerBalance()));
        newStats.put("Median Balance", String.valueOf(getMedianPlayerBalance()));
        newStats.put("Last Tax Balance", String.valueOf(Objects.requireNonNull(Taxation.getInstance().getMainTaxDeposit()).getBalance(VaultType.ALL)));

        ItemEconomy.getInstance().setHistoryStats(newStats);

        for (Account p : ItemEconomy.getInstance().getAccounts().values()) {
            if (p.getAccountType().equals("Player Account"))
                ((PlayerAccount) p).updateSavings();
        }
    }

    public static String getPercentageChange(double now, double before) {
        double diff = now - before;
        double upBy = 0.0;
        if (before != 0)
            upBy = diff / before * 100;
        String percentage = (new DecimalFormat("#.##")).format(upBy);
        StringBuilder s = new StringBuilder();
        s.append(ChatColor.GOLD).append(" ( ");

        if (upBy == 0)
            s.append(ChatColor.YELLOW).append(percentage).append(" %");
        else if (upBy > 0)
            s.append(ChatColor.GREEN).append("↑ ").append(percentage).append(ChatColor.YELLOW).append(" %");
        else if (upBy < 0)
            s.append(ChatColor.RED).append("↓ ").append(percentage).append(ChatColor.YELLOW).append(" %");

        s.append(ChatColor.GOLD).append(" ) ");

        return s.toString();
    }

    public static String getServerStatsMessage() {
        convertAllBalanceBuffers();
        if (!Util.validateHistoryStats(ItemEconomy.getInstance().getHistoryStats()))
            ItemEconomy.getInstance().resetHistoryStats();

        StringBuilder baltopMessage = new StringBuilder();
        baltopMessage.append(ChatColor.GOLD).append("[ItemEconomy] ").append(ChatColor.GREEN).append("Economy Statistics:\n");

        int nowCirc = getTotalCirculation();
        int beforeCirc = Integer.parseInt(ItemEconomy.getInstance().getHistoryStats().get("Circulation"));
        String change = getPercentageChange(nowCirc, beforeCirc);

        baltopMessage.append(ChatColor.YELLOW).append("> ").append(ChatColor.GREEN).append("Total Circulation: ").append(ChatColor.YELLOW).append(nowCirc)
                .append(change).append("\n");

        int nowAvg = getAveragePlayerBalance();
        int beforeAvg = Integer.parseInt(ItemEconomy.getInstance().getHistoryStats().get("Average Balance"));
        change = getPercentageChange(nowAvg, beforeAvg);

        baltopMessage.append(ChatColor.YELLOW).append("> ").append(ChatColor.GREEN).append("Average Balance: ").append(ChatColor.YELLOW).append(nowAvg).
                append(change).append("\n");

        int nowMed = getMedianPlayerBalance();
        int beforeMed = Integer.parseInt(ItemEconomy.getInstance().getHistoryStats().get("Median Balance"));
        change = getPercentageChange(nowMed, beforeMed);

        baltopMessage.append(ChatColor.YELLOW).append("> ").append(ChatColor.GREEN).append("Median Balance: ").append(ChatColor.YELLOW).append(nowMed).
                append(change).append("\n");

        baltopMessage.append(ChatColor.YELLOW).append("> ").append(ChatColor.GREEN).append("Global Player Holdings: \n");

        Map<String, Integer> bals = new HashMap<>();

        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
            if (acc != null && !acc.getID().equals((String)Config.SmartShopConfig.get("smartShopHolderName")))
                bals.put(acc.getName(), acc.getBalance(VaultType.ALL));
        }

        bals = Util.sortByValue(bals);

        List<String> names = new ArrayList<>(bals.keySet());
        int j = 1;
        for (int i = names.size() - 1; i >= 0; i--) {
            String name = names.get(i);
            if (name != null) {
                String rateofchange = " ";

                if (Util.isPlayerName(name)) {
                    PlayerAccount holder = (PlayerAccount) ItemEconomy.getInstance().getAccounts().get(Util.getPlayerID(name));
                    rateofchange = Util.getPercentageBalanceChangeMessage(holder);
                }

                baltopMessage.append("    ").append(j).append(". ").append(ChatColor.GOLD).append(name).append(" ".repeat(24 - name.length()));
                baltopMessage.append(ChatColor.YELLOW).append(bals.get(name)).append(ChatColor.AQUA).append(" ").append(shallowcraft.itemeconomy.Config.currency.name().toLowerCase()).
                        append(rateofchange).append("\n");
                j++;
            }
        }


        return baltopMessage.toString();
    }

    public static boolean validateHistoryStats(Map<String, String> stats) {
        if (stats == null)
            return false;

        String circ = stats.get("Circulation");
        String avgBal = stats.get("Average Balance");
        String medBal = stats.get("Median Balance");

        try {
            Integer.parseInt(circ);
            Integer.parseInt(avgBal);
            Integer.parseInt(medBal);
        } catch (NumberFormatException | NullPointerException e) {
            ItemEconomy.log.info("[ItemEconomy] Bad History States format");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static Transaction convertBalanceBuffer(Account account){
        Transaction result;
        if(account.getBalanceBuffer() >= 1.0) {
            result = TransactionUtils.depositAllVaults((int)account.getBalanceBuffer(), account.getVaults());
            account.updateBalanceBuffer(-1*result.amount);
        } else if(account.getBalanceBuffer() <= -1.0) {
            result = TransactionUtils.withdrawAllVaults((int)(-1*account.getBalanceBuffer()),Util.getAllVaultsBalance(account.getVaults()),account.getVaults());
            account.updateBalanceBuffer(result.amount);
        }
        else {
            result = new Transaction(0, Transaction.ResultType.FAILURE, "balance buffer too small");
        }

        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("[ItemEconomy] Convert Balance Buffer for account with ID: " + account.getID() + "\n Result is: + " + result);

        return result;
    }

    public static void convertAllBalanceBuffers(){
        for (Account acc:ItemEconomy.getInstance().getAccounts().values()) {
            acc.convertBalanceBuffer();
        }
    }

    public static String getBalanceMessage(Account holder){
        holder.convertBalanceBuffer();

        List<Vault> vaults = holder.getVaults();
        int deposit = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.DEPOSIT_ONLY, vaults));
        int withdraw = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.WITHDRAW_ONLY, vaults));
        int regular = Util.getAllVaultsBalance(Util.getVaultsOfType(VaultType.REGULAR, vaults));

        String rateOfChange = "";
        String inventoryBal = "";
        String buffer = ChatColor.GREEN + "Buffer: " + ChatColor.YELLOW + (new DecimalFormat("#.##")).format(holder.getBalanceBuffer()) + ChatColor.GREEN;
        if (holder instanceof PlayerAccount) {
            rateOfChange = Util.getPercentageBalanceChangeMessage((PlayerAccount) holder);
            inventoryBal = ChatColor.GREEN +
                    "Inventory -> " + ChatColor.YELLOW + ((PlayerAccount) holder).getLastInventoryBalance() +
                    ChatColor.GREEN;
        }

        return ChatColor.GOLD + "[ItemEconomy] " + ChatColor.YELLOW + holder.getName() + ChatColor.GREEN + "'s chequing balance is " + ChatColor.YELLOW +
                holder.getBalance(VaultType.REGULAR) + " " + ChatColor.AQUA + "Diamonds." + ChatColor.GREEN + " \n Total Holdings: " + ChatColor.YELLOW + holder.getBalance(VaultType.ALL) +
                rateOfChange + ChatColor.GREEN + "    " + buffer + ChatColor.GREEN
                + "\n" + inventoryBal +
                "\n Vaults ->  Regular: " + ChatColor.YELLOW + regular +
                ChatColor.GREEN + " , Deposit: " + ChatColor.YELLOW + deposit + ChatColor.GREEN + " , Withdraw: " + ChatColor.YELLOW + withdraw
                + ChatColor.GREEN + ".";
    }



//    public static GeneralAccount getGeneralAccountFromUUID(UUID uuid) {
//        String name = Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName();
//        for (Account acc : ItemEconomy.getInstance().getAccounts().values()) {
//            if (acc instanceof GeneralAccount && acc.getID().equals(name)){
//                ItemEconomy.log.info("[ItemEconomy] Found a uuid to general account relation, commencing transfer.");
//                return (GeneralAccount) acc;
//            }
//
//        }
//
//        return null;
//    }


}
