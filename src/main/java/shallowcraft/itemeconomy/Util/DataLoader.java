package shallowcraft.itemeconomy.Util;

import com.google.gson.Gson;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Accounts.TaxAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Vault.ContainerVault;
import shallowcraft.itemeconomy.Vault.Vault;
import shallowcraft.itemeconomy.Vault.VaultType;

import java.io.*;
import java.util.*;

public class DataLoader {

    public static List<Account> loadJSON(File dataFile, Server server) throws IOException, InvalidDataException, NullPointerException {
        List<Account> accounts = new ArrayList<>();

        Gson gson = new Gson();
        FileReader reader = new FileReader(dataFile);
        Map<String, Map<String, String>> jsonData = gson.fromJson(reader, Map.class);
        reader.close();

        if(jsonData == null || jsonData.isEmpty())
            throw new InvalidDataException("[ItemEconomy] Failed to load data due to invalid file.");

        for (String id : jsonData.keySet()) {
            Map<String, String> inputData = jsonData.get(id);

            Material currency = Material.getMaterial(inputData.get("currency"));
            assert currency != null;
            //ItemEconomy.log.info("[ItemEconomy] Loaded currency with material ID: " + currency.name());

            if(id.equals(Config.taxID)){
                double taxBuffer = Double.parseDouble(inputData.get("Tax Buffer"));

                Account acc = new TaxAccount(taxBuffer);
                populateAccount(acc, currency, inputData, server);
                accounts.add(acc);

                //ItemEconomy.log.info("[ItemEconomy] Loaded data for account with ID: " + id);
            } else {
                OfflinePlayer player = server.getOfflinePlayer(UUID.fromString(id));

                if(player != null){
                    //ItemEconomy.log.info("[ItemEconomy] Loaded data for account with ID: " + id);

                    int personalBalance = 0;
                    personalBalance = Integer.parseInt(inputData.get("personal_balance"));
                    //ItemEconomy.log.info("[ItemEconomy] Loaded last known personal balance of " + personalBalance + " Diamonds");

                    Account acc = new PlayerAccount(player, currency, personalBalance);
                    populateAccount(acc, currency, inputData, server);
                    accounts.add(acc);
                } else
                    ItemEconomy.log.info("[ItemEconomy] Failed to load data for account with ID: " + id);
            }
        }

        return accounts;
    }

    public static void saveDataToJSON(List<Account> accounts, File dataFile) throws IOException {
        Gson gson = new Gson();
        Map<String, Map<String, String>> output = new HashMap<>();

        for (Account acc : accounts) {
            Map<String, String> outputData = new HashMap<>();

            String id = acc.getID();
            //ItemEconomy.log.info("[ItemEconomy] Saving account with ID: " + id);

            if(acc instanceof PlayerAccount){
                PlayerAccount playerAccount = (PlayerAccount) acc;

                String personalBalance = String.valueOf(playerAccount.getLastPersonalBalance());
                outputData.put("personal_balance", personalBalance);
               // ItemEconomy.log.info("[ItemEconomy] Saving balance");
            } else if (acc instanceof TaxAccount){
                String taxBuffer = String.valueOf(((TaxAccount) acc).taxBuffer);
                outputData.put("Tax Buffer", taxBuffer);
                //ItemEconomy.log.info("[ItemEconomy] Saving tax buffer");
            }


            String currency = acc.getItemCurrency().toString();
            outputData.put("currency", currency);
            //ItemEconomy.log.info("[ItemEconomy] Saving currency");

            int containerIndex = 0;
            for (Vault vault : acc.getVaults()) {
                if(vault.checkVault()){
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

            output.put(id, outputData);
        }

        //ItemEconomy.log.info("[ItemEconomy] output data: " + output.toString());

        FileWriter writer = new FileWriter(dataFile);
        gson.toJson(output, writer);
        writer.close();
    }

    private static void populateAccount(Account currentAccount, Material currency, Map<String, String> inputData, Server server){
        List<Vault> vaults = new ArrayList<>();

        int containerIndex = 0;

        for (String identifier : inputData.keySet()) {

            if (identifier.contains("Container")) {
                //ItemEconomy.log.info("[ItemEconomy] Loading Vault " + containerIndex);
                containerIndex++;

                String[] data = inputData.get(identifier).split(",");

                Location signLoc = new Location(server.getWorld(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                Location containerLoc = new Location(server.getWorld(data[0]), Integer.parseInt(data[4]), Integer.parseInt(data[5]), Integer.parseInt(data[6]));

                VaultType type = VaultType.REGULAR;
                if(data.length > 7)
                    type = VaultType.fromID(Integer.parseInt(data[7]));

                Block sign = signLoc.getBlock();
                Block container = containerLoc.getBlock();
                if (Util.isValidContainer(container.getType()) && Util.isValidVaultSign((Sign) sign.getState())) {
                    Vault currentVault = new ContainerVault(container, (Sign) sign.getState(), currentAccount, currency, type);
                    vaults.add(currentVault);
                }
            }
        }

        currentAccount.overrideLoadVaults(vaults);
    }

    public static File createDataFile(String fileName) throws IOException {
        File dataFile = new File("plugins/ItemEconomy/" + fileName + ".json");
        File dir = dataFile.getParentFile(); // Get the parent directory
        dir.mkdirs(); // Creates all directories that do not exist
        dataFile.createNewFile(); // Creates a new file if it does not already exist; throws IOException

        return  dataFile;
    }

    public static File getDataFile(String fileName) throws IOException{
        return  new File("plugins/ItemEconomy/" + fileName + ".json");
    }




}
