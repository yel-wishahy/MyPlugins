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
                int toRemove = Util.amountToRemove(stack.getAmount(), amount - numRemoved);
                stack.setAmount(stack.getAmount() - toRemove);
                numRemoved += toRemove;
            }

            if (stack != null && stack.getType().equals(Config.currency_block)) {
                ItemEconomy.log.info("withdrawing blocks");
                int toRemove = Util.amountToRemove(stack.getAmount() * 9, amount - numRemoved);
                int[] result = Util.currencyToCurrencyBlock(toRemove);

                int itemsToRemove = result[0];
                int blocksToRemove = result[1];
                int toConvert = 0;

                if(itemsToRemove > 0)
                    toConvert = (int) Math.ceil(((double) itemsToRemove)/9.0);

                ItemStack itemStack = null;
                if(toConvert > 0 && stack.getAmount() - toConvert >= blocksToRemove)
                    itemStack = Util.convertToItem(toConvert, stack, inventory);

                if(itemStack != null && stack.getAmount() >= blocksToRemove && itemStack.getAmount() >= itemsToRemove){
                    stack.setAmount(stack.getAmount() - blocksToRemove);
                    itemStack.setAmount(itemStack.getAmount() - itemsToRemove);
                    numRemoved += itemsToRemove + blocksToRemove * 9;
                }
            }
        }

        if(numRemoved == amount){
            ItemEconomy.log.info("Success: Withdrew " + numRemoved + " from " + inventory.getType().name());
            return new TransactionResult(numRemoved, TransactionResult.ResultType.SUCCESS, "withdraw");
        }

        return new TransactionResult(numRemoved, TransactionResult.ResultType.FAILURE, "withdraw");
    }

    public static TransactionResult deposit(Inventory inventory, int amount){
        if(amount == 0)
            return new TransactionResult(0, TransactionResult.ResultType.SUCCESS, "deposit");

        ItemEconomy.log.info("Trying to deposit " + amount + " into " + inventory.getType().name());
        int numAdded = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            if(numAdded >= amount)
                break;

            ItemStack item = inventory.getItem(i);

            int[] conversion = Util.currencyToCurrencyBlock(amount - numAdded);
            int itemsToAdd = conversion[0];
            int blocksToAdd = conversion[1];

            if(item == null){
                int toAdd = 0;

                if(blocksToAdd > 0){
                    toAdd = Util.amountToAdd(0, blocksToAdd);
                    inventory.setItem(i, new ItemStack(Config.currency_block, toAdd));
                    numAdded += blocksToAdd * 9;
                } else {
                    toAdd = Util.amountToAdd(0, itemsToAdd);
                    inventory.setItem(i, new ItemStack(Config.currency, toAdd));
                    numAdded += toAdd;
                }
            } else if(item.getType().equals(Config.currency_block)){
                int toAdd = Util.amountToAdd(item.getAmount(), blocksToAdd);
                item.setAmount(item.getAmount() + toAdd);
                numAdded+= toAdd * 9;
            } else if(item.getType().equals(Config.currency)){
                int toAdd = Util.amountToAdd(item.getAmount(), itemsToAdd);
                item.setAmount(item.getAmount() + toAdd);
                numAdded+= toAdd;
            }
        }

        if(numAdded == amount){
            ItemEconomy.log.info("Success: Deposited " + numAdded + " into " + inventory.getType().name());
            return new TransactionResult(numAdded, TransactionResult.ResultType.SUCCESS, "deposit");
        }

        return new TransactionResult(numAdded, TransactionResult.ResultType.INSUFFICIENT_SPACE, "deposit");
    }
}
