package shallowcraft.itemeconomy.Core;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Util.Util;

import java.util.Objects;

public class Transaction {
    public static TransactionResult withdraw(Inventory inventory, int amount) {
        if(amount == 0)
            return new TransactionResult(0, TransactionResult.ResultType.SUCCESS, "withdraw");

        ItemEconomy.log.info("Trying to withdraw " + amount + " from " + inventory.getType().name());
        int numRemoved = 0;

        for (ItemStack stack : inventory) {
            if (numRemoved >= amount)
                break;

            if (stack != null && stack.getType().equals(Config.currency)) {
                ItemEconomy.log.info("withdrawing items");
                int toRemove = Util.amountToRemove(stack.getAmount(), amount);
                stack.setAmount(stack.getAmount() - toRemove);
                numRemoved += toRemove;
            }

            if (stack != null && stack.getType().equals(Config.currency_block)) {
                ItemEconomy.log.info("withdrawing blocks");
                int toRemove = Util.amountToRemove(stack.getAmount() * 9, amount);
                int[] result = Util.currencyToCurrencyBlock(toRemove);

                int items = result[0];
                int blocks = result[1];

                int slot = inventory.firstEmpty();
                if (slot != -1) {
                    inventory.setItem(slot, new ItemStack(Config.currency, items));
                }

                stack.setAmount(stack.getAmount() - blocks);
                numRemoved += items + blocks * 9;
            }
        }

        ItemEconomy.log.info("Success: Withdrew " + numRemoved + " from " + inventory.getType().name());
        return new TransactionResult(numRemoved, TransactionResult.ResultType.SUCCESS, "withdraw");
    }

    public static TransactionResult deposit(Inventory inventory, int amount){
        if(amount == 0)
            return new TransactionResult(0, TransactionResult.ResultType.SUCCESS, "deposit");

        ItemEconomy.log.info("Trying to deposit " + amount + " into " + inventory.getType().name());
        int numAdded = 0;

        while(numAdded < amount){
            boolean empty = false;
            int slot = inventory.first(Config.currency);
            if(slot == -1){
                slot = inventory.firstEmpty();
                empty = true;
            }


            if(slot == -1){
                ItemEconomy.log.info("Failed: only able to deposit " + numAdded + " into " + inventory.getType().name());
                return new TransactionResult(numAdded, TransactionResult.ResultType.INSUFFICIENT_SPACE, "deposit");
            }


            int toAdd = 0;

            if(empty) {
                toAdd = Util.amountToAdd(0, amount - numAdded);
                inventory.setItem(slot, new ItemStack(Config.currency, toAdd));
            }
            else {
                toAdd = Util.amountToAdd(Objects.requireNonNull(inventory.getItem(slot)).getAmount(), amount-numAdded);
                Objects.requireNonNull(inventory.getItem(slot)).setAmount(Objects.requireNonNull(inventory.getItem(slot)).getAmount() + toAdd);
            }

            numAdded+=toAdd;
        }

        ItemEconomy.log.info("Success: Deposited " + numAdded + " into " + inventory.getType().name());
        return new TransactionResult(numAdded, TransactionResult.ResultType.SUCCESS, "deposit");
    }
}
