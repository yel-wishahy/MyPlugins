package shallowcraft.itemeconomy.Data;

import com.google.gson.Gson;
import shallowcraft.itemeconomy.Accounts.Account;
import shallowcraft.itemeconomy.Accounts.GeneralAccount;
import shallowcraft.itemeconomy.Accounts.PlayerAccount;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.ThirdPartyIntegration.smartshop.ShopOrder.ShopOrder;
import shallowcraft.itemeconomy.Util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    public static Map<String, Account> loadDataFromJSON(File dataFile) throws IOException, InvalidDataException, NullPointerException {
        Map<String, Account> accounts = new HashMap<>();

        Gson gson = new Gson();
        FileReader reader = new FileReader(dataFile);
        Map<String, Map<String, String>> jsonData = gson.fromJson(reader, Map.class);
        reader.close();

        if (jsonData == null || jsonData.isEmpty())
            throw new InvalidDataException("[ItemEconomy] Failed to load data due to invalid file.");

        for (String key : jsonData.keySet()) {
            Map<String, String> inputData = jsonData.get(key);
            if(key.equals("Stats")){
                String circ = inputData.get("Circulation");
                String avg = inputData.get("Average Balance");
                String median = inputData.get("Median Balance");
                String lastTaxBal = "0";

                try{
                    lastTaxBal = inputData.get("Last Tax Balance");
                } catch (Exception ignored){}

                Map<String, String> newStats = new HashMap<>();
                newStats.put("Circulation", circ);
                newStats.put("Average Balance", avg);
                newStats.put("Median Balance", median);
                newStats.put("Last Tax Balance", lastTaxBal);

                if(!Util.validateHistoryStats(newStats)) {
                    newStats = new HashMap<>();
                    newStats.put("Circulation", "0");
                    newStats.put("Average Balance", "0");
                    newStats.put("Median Balance", "0");
                    newStats.put("Last Tax Balance", "0");
                }

                ItemEconomy.getInstance().setHistoryStats(newStats);

            } else {

                String id = key.split(",")[1];
                String type = key.split(",")[0];

                //ItemEconomy.log.info("[ItemEconomy] Loaded currency with material ID: " + currency.name());

                if (type.equals("General Account")) {
                    Account acc = new GeneralAccount(inputData, id);
                    accounts.put(acc.getID(), acc);

                    //ItemEconomy.log.info("[ItemEconomy] Loaded data for account with ID: " + id);
                } else if (type.equals("Player Account")) {
                    Account acc = new PlayerAccount(inputData, id);
                    accounts.put(acc.getID(), acc);
                } else
                    ItemEconomy.log.info("[ItemEconomy] Failed to load data for account with ID: " + id);
            }
        }


        return accounts;
    }

    public static void saveDataToJSON(Map<String, Account> accounts, File dataFile) throws IOException {
        Gson gson = new Gson();
        Map<String, Map<String, String>> output = new HashMap<>();

        for (Account acc : accounts.values()) {
            String id = acc.getAccountType() + "," + acc.getID();
            Map<String, String> outputData = acc.getSerializableData();

            output.put(id, outputData);
        }

        output.put("Stats",ItemEconomy.getInstance().getHistoryStats());

        //ItemEconomy.log.info("[ItemEconomy] output data: " + output.toString());

        FileWriter writer = new FileWriter(dataFile);
        gson.toJson(output, writer);
        writer.close();
    }

    public static File createDataFileJSON(String fileName) throws IOException {
        File dataFile = new File("plugins/ItemEconomy/" + fileName + ".json");
        File dir = dataFile.getParentFile(); // Get the parent directory
        dir.mkdirs(); // Creates all directories that do not exist
        dataFile.createNewFile(); // Creates a new file if it does not already exist; throws IOException

        return dataFile;
    }

    public static File createDataFileYML(String fileName) throws IOException {
        File dataFile = new File("plugins/ItemEconomy/" + fileName + ".yml");
        File dir = dataFile.getParentFile(); // Get the parent directory
        dir.mkdirs(); // Creates all directories that do not exist
        dataFile.createNewFile(); // Creates a new file if it does not already exist; throws IOException

        return dataFile;
    }

    public static File getDataFile(String fileName) {
        return new File("plugins/ItemEconomy/" + fileName + ".json");
    }

    public static FileInputStream getDataStreamYML(String filename) throws FileNotFoundException {
        return new FileInputStream("plugins/ItemEconomy/" + Config.configFileName + ".yml");
    }

    public static Map<String, List<ShopOrder>> loadShopOrdersFromJSON(File dataFile) throws IOException, InvalidDataException, NullPointerException {
        Map<String, List<ShopOrder>> output = new HashMap<>();

        Gson gson = new Gson();
        FileReader reader = new FileReader(dataFile);
        Map<String, Map<String, Map<String,String>>> jsonData = gson.fromJson(reader, Map.class);
        reader.close();

        if (jsonData == null || jsonData.isEmpty())
            throw new InvalidDataException("[ItemEconomy] Failed to load shop order data due to invalid file.");

        for (String id:jsonData.keySet()) {
            List<ShopOrder> shopOrders = new ArrayList<>();

            Map<String, Map<String,String>> inputData = jsonData.get(id);

            for (Map<String,String> data:inputData.values()) {
                ShopOrder s = new ShopOrder(data);
                if(!s.isStale())
                    shopOrders.add(s);
            }


            output.put(id, shopOrders);
        }

        return output;
    }

    public static void saveShopOrdersToJSON(Map<String, List<ShopOrder>> data, File dataFile) throws IOException {
        Gson gson = new Gson();
        Map<String, Map<String, Map<String,String>>> output = new HashMap<>();

        for (String id:data.keySet()) {
            Map<String, Map<String,String>> orders = new HashMap<>();

            int index = 0;
            for (ShopOrder shopOrder:data.get(id)) {
                orders.put("Shop_Order_"+index, shopOrder.getSerializableData());
                index++;
            }

            output.put(id, orders);
        }

        //ItemEconomy.log.info("[ItemEconomy] output data: " + output.toString());

        FileWriter writer = new FileWriter(dataFile);
        gson.toJson(output, writer);
        writer.close();
    }

    public static void saveShopOrderLogsToJSON(Map<String, List<String>> data, File dataFile) throws IOException{
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(dataFile);
        gson.toJson(data, writer);
        writer.close();
    }

    public static Map<String, List<String>> loadShopOrderLogsFromJSON(File dataFile) throws IOException, InvalidDataException, NullPointerException{
        Gson gson = new Gson();
        FileReader reader = new FileReader(dataFile);
        Map<String, List<String>> inputData = gson.fromJson(reader, Map.class);
        reader.close();

        if (inputData == null || inputData.isEmpty())
            throw new InvalidDataException("[ItemEconomy] Failed to load shop order logs due to invalid file.");

        return inputData;
    }
}
