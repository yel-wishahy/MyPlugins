package shallowcraft.itemeconomy.Transaction;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.BankVault.Vault;
import shallowcraft.itemeconomy.BankVault.VaultType;
import shallowcraft.itemeconomy.ItemEconomy;
import shallowcraft.itemeconomy.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class TransactionUtils {

    public static Transaction withdraw(Inventory inventory, int amount) {
        if(amount == 0)
            return new Transaction(0, Transaction.ResultType.SUCCESS, "withdraw");
        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("Trying to withdraw " + amount + "from " + inventory.getType().name() + " with total of " + Util.countItem(inventory));
        int numRemoved = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (numRemoved >= amount)
                break;

            ItemStack stack = inventory.getItem(i);

            if (stack != null && stack.getType().equals(Config.currency)) {
                //ItemEconomy.log.info(" start stack is" + (stack.getAmount()));
                int toRemove = Util.amountToRemove(stack.getAmount(), amount - numRemoved);
                //ItemEconomy.log.info("items: " + toRemove);
                stack.setAmount(stack.getAmount() - toRemove);
                inventory.setItem(i,stack);
                numRemoved += toRemove;
               // ItemEconomy.log.info("stack is" + (stack.getAmount()));
            } else if (stack != null && stack.getType().equals(Config.currency_block)) {
                //ItemEconomy.log.info(" start stack is" + (stack.getAmount()));
                int toRemove = Util.amountToRemove(stack.getAmount() * 9, amount - numRemoved);
                int[] result = Util.currencyToCurrencyBlock(toRemove);

                int itemsToRemove = result[0];
                int blocksToRemove = result[1];
                int toConvert = 0;
                //ItemEconomy.log.info("items: " + itemsToRemove + " blocks: " + blocksToRemove);

                if(itemsToRemove > 0)
                    toConvert = (int) Math.ceil(itemsToRemove/9.0);

                //ItemEconomy.log.info("to convert " + toConvert);

                int itemStack = -1;
                if(toConvert > 0) {
                    if (stack.getAmount() - toConvert < blocksToRemove && blocksToRemove >= toConvert)
                        blocksToRemove -= toConvert;
                    itemStack = Util.getSlotToConvertToItem(toConvert, stack, i, inventory);
                }

                if(itemStack != -1 && stack.getAmount() >= blocksToRemove){
                    stack.setAmount(stack.getAmount() - blocksToRemove);
                    inventory.setItem(i, stack);
                    inventory.setItem(itemStack, new ItemStack(Config.currency, toConvert * 9 - itemsToRemove));
                    numRemoved += itemsToRemove + blocksToRemove * 9;
                } else if (itemsToRemove == 0 && stack.getAmount() >= blocksToRemove) {
                    stack.setAmount(stack.getAmount() - blocksToRemove);
                    inventory.setItem(i, stack);
                    numRemoved += itemsToRemove + blocksToRemove * 9;
                }

                //ItemEconomy.log.info("stack is" + (stack.getAmount()));
            }
        }

        if(numRemoved == amount){
            if(ItemEconomy.getInstance().isDebugMode())
                ItemEconomy.log.info("success NOW HAS:  " + Util.countItem(inventory));
            return new Transaction(numRemoved, Transaction.ResultType.SUCCESS, "withdraw");
        }
        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("fail NOW HAS:  " + Util.countItem(inventory));
        return new Transaction(numRemoved, Transaction.ResultType.FAILURE, "withdraw");
    }


    public static Transaction deposit(Inventory inventory, int amount){
        if(amount == 0)
            return new Transaction(0, Transaction.ResultType.SUCCESS, "deposit");
        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("Trying to deposit " + amount + " into " + inventory.getType().name() + " with total of " + Util.countItem(inventory));
        int numAdded = 0;


        for (int i = 0; i < inventory.getSize(); i++) {
            if(numAdded >= amount)
                break;

            int toAdd = 0;

            ItemStack item = inventory.getItem(i);

            int[] conversion = Util.currencyToCurrencyBlock(amount - numAdded);
            int itemsToAdd = conversion[0];
            int blocksToAdd = conversion[1];
            //ItemEconomy.log.info("items: " + itemsToAdd + " blocks: " + blocksToAdd);

            if(item == null){
                if(blocksToAdd > 0){
                    toAdd = Util.amountToAdd(0, blocksToAdd);
                    inventory.setItem(i, new ItemStack(Config.currency_block, toAdd));
                    numAdded += toAdd * 9;
                } else {
                    toAdd = Util.amountToAdd(0, itemsToAdd);
                    inventory.setItem(i, new ItemStack(Config.currency, toAdd));
                    numAdded += toAdd;
                }
            } else if(item.getType().equals(Config.currency_block)){
                toAdd = Util.amountToAdd(item.getAmount(), blocksToAdd);
                item.setAmount(item.getAmount() + toAdd);
                numAdded+= toAdd * 9;
            } else if(item.getType().equals(Config.currency)){
                toAdd = Util.amountToAdd(item.getAmount(), itemsToAdd);
                item.setAmount(item.getAmount() + toAdd);
                numAdded+= toAdd;
            }
        }

        if(numAdded == amount){
            if(ItemEconomy.getInstance().isDebugMode()) {
                ItemEconomy.log.info("Success: Deposited " + numAdded + " into " + inventory.getType().name());
                ItemEconomy.log.info("NOW HAS:  " + Util.countItem(inventory));
            }
            return new Transaction(numAdded, Transaction.ResultType.SUCCESS, "deposit");
        }
        if(ItemEconomy.getInstance().isDebugMode())
            ItemEconomy.log.info("NOW HAS:  " + Util.countItem(inventory));

        return new Transaction(numAdded, Transaction.ResultType.INSUFFICIENT_SPACE, "deposit");
    }

    public static Transaction depositAllVaults(int amount, List<Vault> vaults){
        if(amount < 1)
            return new Transaction(0, Transaction.ResultType.SUCCESS, "deposit");

        int numAdded = 0;

        for (Vault vault:new ArrayList<>(vaults)) {
            if(numAdded >= amount)
                break;

            if(vault.getVaultType() == VaultType.DEPOSIT_ONLY)
                numAdded+=vault.deposit(amount - numAdded).amount;
        }

        for (Vault vault:new ArrayList<>(vaults)) {
            if(numAdded >= amount)
                break;

            if(vault.getVaultType() != VaultType.WITHDRAW_ONLY)
                numAdded+=vault.deposit(amount - numAdded).amount;
        }

        if(numAdded < amount)
            return new Transaction(numAdded, Transaction.ResultType.INSUFFICIENT_SPACE, "deposit");

        return new Transaction(numAdded, Transaction.ResultType.SUCCESS, "deposit");
    }

    public static Transaction withdrawAllVaults(int amount, int currentBalance, List<Vault> vaults){
        if(currentBalance < amount)
            return new Transaction(0, Transaction.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        int numRemoved = 0;

        for (Vault vault:new ArrayList<>(vaults)) {
            if(numRemoved >= amount)
                break;

            if(vault.getVaultType() == VaultType.WITHDRAW_ONLY){
                int toRemove = Util.amountToRemove(vault.getVaultBalance(), amount - numRemoved);
                numRemoved += vault.withdraw(toRemove).amount;
            }
        }

        for (Vault vault:new ArrayList<>(vaults)) {
            if(numRemoved >= amount)
                break;
            if(vault.getVaultType() != VaultType.DEPOSIT_ONLY){
                int toRemove = Util.amountToRemove(vault.getVaultBalance(), amount - numRemoved);
                numRemoved += vault.withdraw(toRemove).amount;
            }
        }


        if(numRemoved < amount)
            return new Transaction(numRemoved, Transaction.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        return new Transaction(numRemoved, Transaction.ResultType.SUCCESS, "withdraw");
    }

    public static Transaction forceWithdrawAllVaults(int amount, int currentBalance, List<Vault> vaults){
        if(currentBalance < amount)
            return new Transaction(0, Transaction.ResultType.INSUFFICIENT_FUNDS, "withdraw");


        int numRemoved = 0;

        for (Vault vault:new ArrayList<>(vaults)) {
            if(numRemoved >= amount)
                break;

            int toRemove = Util.amountToRemove(vault.getVaultBalance(), amount - numRemoved);
            numRemoved += vault.withdraw(toRemove).amount;
        }


        if(numRemoved < amount)
            return new Transaction(numRemoved, Transaction.ResultType.INSUFFICIENT_FUNDS, "withdraw");

        return new Transaction(numRemoved, Transaction.ResultType.SUCCESS, "withdraw");
    }

    private static Transaction transferVault(Vault source, Vault destination, int amount){
        if(source.getVaultBalance() < amount)
            return new Transaction(0, Transaction.ResultType.INSUFFICIENT_FUNDS, "insufficient funds");

        Transaction result = source.withdraw(amount);
        result = destination.deposit(result.amount);

        return result;
    }

    public static Transaction transferVaults(List<Vault> sources, List<Vault> destinations, int amount){
        int numTransferred = 0;

        for (Vault s:sources) {
            if(numTransferred >= amount)
                break;
            for (Vault d:destinations) {
                if(numTransferred >= amount)
                    break;

                Transaction result = TransactionUtils.transferVault(s, d, amount - numTransferred);
                numTransferred += result.amount;
            }
        }

        if(numTransferred == amount)
            return new Transaction(numTransferred, Transaction.ResultType.SUCCESS, "transfer");
        else
            return new Transaction(numTransferred, Transaction.ResultType.FAILURE, "transfer");
    }
}
