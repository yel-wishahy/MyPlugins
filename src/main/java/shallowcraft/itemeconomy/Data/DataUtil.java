package shallowcraft.itemeconomy.Data;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomyPlugin;
import shallowcraft.itemeconomy.Tax.taxable.GeneralTax;
import shallowcraft.itemeconomy.Tax.taxable.Taxable;
import shallowcraft.itemeconomy.BankVault.ContainerVault;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;

import java.util.*;

public class DataUtil {

    public static void populateAccount(Account currentAccount, Map<String, String> inputData, Server server) {
        List<Vault> vaults = new ArrayList<>();
        Map<String, GeneralTax> taxes = new HashMap<>();

        for (String identifier : inputData.keySet()) {
            if (identifier.contains("Container")) {
                //ItemEconomy.log.info("[ItemEconomy] Loading Vault " + containerIndex);
                String[] data = inputData.get(identifier).split(",");

                Location signLoc = new Location(server.getWorld(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                Location containerLoc = new Location(server.getWorld(data[0]), Integer.parseInt(data[4]), Integer.parseInt(data[5]), Integer.parseInt(data[6]));

                VaultType type = VaultType.REGULAR;
                if (data.length > 7)
                    type = VaultType.fromID(Integer.parseInt(data[7]));

                Block sign = signLoc.getBlock();
                Block container = containerLoc.getBlock();

                Vault currentVault = new ContainerVault(container, (Sign) sign.getState(), currentAccount, type);
                vaults.add(currentVault);
            } else if (identifier.contains("Tax") && currentAccount instanceof PlayerAccount) {
                PlayerAccount holder = (PlayerAccount) currentAccount;
                String[] data = inputData.get(identifier).split(",");
                GeneralTax tax = null;

                String name = data[0];
                double rate = Double.parseDouble(data[1]);
                Date last = null;
                Date next = null;
                try {
                    last = Config.timeFormat.parse(data[2]);
                    next = Config.timeFormat.parse(data[3]);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }

                if (last != null && next != null)
                    tax = new GeneralTax(holder, name, rate, last, next);
                else
                    tax = new GeneralTax(holder, name, rate);

                taxes.put(tax.getTaxName(), tax);
            }
        }

        currentAccount.overrideLoadVaults(vaults);

        if (currentAccount instanceof PlayerAccount)
            ((PlayerAccount) currentAccount).overrideLoadTaxes(taxes);
    }

    public static void logVaults(Account acc, Map<String, String> outputData) {
        int containerIndex = 0;
        for (Vault vault : acc.getVaults()) {
            if (vault.checkVault()) {
                //ItemEconomy.log.info("[ItemEconomy] Saving Vault " + containerIndex + " of " + acc.getVaults().size());
                StringBuilder data = new StringBuilder();
                String worldName = vault.getSign().getLocation().getWorld().getName();
                data.append(worldName).append(",");

                int signx = vault.getSign().getLocation().getBlockX();
                data.append(signx).append(",");
                int signy = vault.getSign().getLocation().getBlockY();
                data.append(signy).append(",");
                int signz = vault.getSign().getLocation().getBlockZ();
                data.append(signz).append(",");

                int containerx = vault.getContainer().getLocation().getBlockX();
                data.append(containerx).append(",");
                int containery = vault.getContainer().getLocation().getBlockY();
                data.append(containery).append(",");
                int containerz = vault.getContainer().getLocation().getBlockZ();
                data.append(containerz).append(",").append(vault.getVaultType().getId());

                outputData.put("Container_" + containerIndex, data.toString());
                containerIndex++;
            }
        }
    }

    public static void logTaxes(PlayerAccount acc, Map<String, String> outputData) {
        int index = 0;
        for (Taxable tax : acc.getTaxes().values()) {
            //ItemEconomy.log.info("[ItemEconomy] Saving Vault " + containerIndex + " of " + acc.getVaults().size());
            StringBuilder data = new StringBuilder();
            String taxName = tax.getTaxName();
            String lastTax = Config.timeFormat.format(tax.getLastTaxTime());
            String nextTax = Config.timeFormat.format(tax.getNextTaxTime());
            String taxRate = String.valueOf(tax.getTaxRate());
            data.append(taxName).append(",").append(taxRate).append(",").append(lastTax).append(",").append(nextTax);

            outputData.put("Tax_" + index, data.toString());
            index++;
        }
    }

    public static String serializeLocation(Location location){
        StringBuilder data = new StringBuilder();
        String worldName = location.getWorld().getName();
        data.append(worldName).append(",");

        int x = location.getBlockX();
        data.append(x).append(",");
        int y = location.getBlockY();
        data.append(y).append(",");
        int z = location.getBlockZ();
        data.append(z);

        return data.toString();
    }

    public static Location deserializeLocation(String[] data){
        return new Location(ItemEconomyPlugin.getInstance().getServer().getWorld(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
    }


}