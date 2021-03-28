package shallowcraft.itemeconomy;

import com.google.gson.Gson;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class DataLoader {

    public static List<Account> loadJSON(File dataFile, Server server) throws IOException, InvalidDataException, NullPointerException {
        List<Account> accounts = new ArrayList<>();

        Gson gson = new Gson();
        FileReader reader = new FileReader(dataFile);
        Map<String, Map<String, String>> jsonData = gson.fromJson(reader, Map.class);
        reader.close();

        for (String uuid : jsonData.keySet()) {
            Map<String, String> playerData = jsonData.get(uuid);
            Material currency = Material.getMaterial(playerData.get("currency"));
            OfflinePlayer player = server.getOfflinePlayer(UUID.fromString(uuid));
            ItemEconomy.log.info("[ItemEconomy] Loaded data for player with uuid: " + player.getUniqueId());
            assert currency != null;
            ItemEconomy.log.info("[ItemEconomy] Loaded currency with material ID: " + currency.name());

            Account currentAccount = new Account(player, currency);

            int index = 0;

            for (String identifier : playerData.keySet()) {

                if (identifier.contains("Container")) {
                    index = Integer.parseInt(identifier.split("_")[1]);

                    String[] data = playerData.get(identifier).split(",");

                    Location signLoc = new Location(server.getWorld(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                    Location containerLoc = new Location(server.getWorld(data[0]), Integer.parseInt(data[4]), Integer.parseInt(data[5]), Integer.parseInt(data[6]));


                    Block sign = signLoc.getBlock();
                    Block container = containerLoc.getBlock();
                    if (Util.isValidContainer(container.getType()) && Util.isValidVaultSign((Sign) sign.getState())) {
                        ItemVault currentVault = new ItemVault(container, (Sign) sign.getState(), currentAccount, currency);
                        currentAccount.addVault(currentVault);
                    }
                }
            }

            accounts.add(currentAccount);
        }

        return accounts;
    }

    public static void saveDataToJSON(List<Account> accounts, File dataFile) throws IOException {
        Gson gson = new Gson();
        Map<String, Map<String, String>> output = new HashMap<>();

        for (Account acc : accounts) {
            Map<String, String> playerData = new HashMap<>();

            String uuid = acc.getPlayer().getUniqueId().toString();
            ItemEconomy.log.info("[ItemEconomy] Saving player uuid");
            String currency = acc.getItemCurrency().toString();
            playerData.put("currency", currency);
            ItemEconomy.log.info("[ItemEconomy] Saving Currency");

            int count = 0;
            for (ItemVault vault : acc.getVaults()) {
                ItemEconomy.log.info("[ItemEconomy] Saving Vault " + count + " of " + acc.getVaults().size());
                StringBuilder data = new StringBuilder();
                String worldName = vault.vaultSign.getLocation().getWorld().getName();
                data.append(worldName).append(",");

                int signx = vault.vaultSign.getLocation().getBlockX();
                data.append(signx).append(",");
                int signy = vault.vaultSign.getLocation().getBlockY();
                data.append(signy).append(",");
                int signz = vault.vaultSign.getLocation().getBlockZ();
                data.append(signz).append(",");

                int containerx = vault.containerVault.getLocation().getBlockX();
                data.append(containerx).append(",");
                int containery = vault.containerVault.getLocation().getBlockY();
                data.append(containery).append(",");
                int containerz = vault.containerVault.getLocation().getBlockZ();
                data.append(containerz);

                playerData.put("Container_" + count, data.toString());
                count++;
            }

            output.put(uuid, playerData);
        }

        ItemEconomy.log.info("[ItemEconomy] output data: " + output.toString());

        FileWriter writer = new FileWriter(dataFile);
        gson.toJson(output, writer);
        writer.close();



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
