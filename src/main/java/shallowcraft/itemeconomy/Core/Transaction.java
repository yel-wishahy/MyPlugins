package shallowcraft.itemeconomy.Core;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import shallowcraft.itemeconomy.Config;
import shallowcraft.itemeconomy.Util.Util;

public class Transaction {

    public static TransactionResult withdraw(Inventory inventory, int amount) {
        if(amount == 0)
            return new TransactionResult(0, TransactionResult.ResultType.SUCCESS, "withdraw");

        //ItemEconomy.log.info("Trying to withdraw " + amount + "from " + inventory.getType().name() + " with total of " + Util.countItem(inventory));
        int numRemoved = 0;

        for (ItemStack stack : inventory) {
            if (numRemoved >= amount)
                break;

            if (stack != null && stack.getType().equals(Config.currency)) {
                int toRemove = Util.amountToRemove(stack.getAmount(), amount - numRemoved);
                stack.setAmount(stack.getAmount() - toRemove);
                numRemoved += toRemove;
            }

            if (stack != null && stack.getType().equals(Config.currency_block)) {
                int toRemove = Util.amountToRemove(stack.getAmount() * 9, amount - numRemoved);
                int[] result = Util.currencyToCurrencyBlock(toRemove);

                int itemsToRemove = result[0];
                int blocksToRemove = result[1];
                int toConvert = 0;

                if(itemsToRemove > 0)
                    toConvert = (int) Math.ceil(((double) itemsToRemove)/9.0);


                ItemStack itemStack = null;
                if(toConvert > 0){
                    blocksToRemove -=  toConvert;
                    itemStack = Util.convertToItem(toConvert, stack, inventory);
                }

                if(itemStack != null && stack.getAmount() >= blocksToRemove && itemStack.getAmount() >= itemsToRemove){
                    stack.setAmount(stack.getAmount() - blocksToRemove);
                    itemStack.setAmount(itemStack.getAmount() - itemsToRemove);
                    numRemoved += itemsToRemove + blocksToRemove * 9;
                } else if (itemsToRemove == 0 && stack.getAmount() >= blocksToRemove) {
                    stack.setAmount(stack.getAmount() - blocksToRemove);
                    numRemoved += itemsToRemove + blocksToRemove * 9;
                }
            }
        }

        if(numRemoved == amount){
            //ItemEconomy.log.info("NOW HAS:  " + Util.countItem(inventory));
            return new TransactionResult(numRemoved, TransactionResult.ResultType.SUCCESS, "withdraw");
        }

        //ItemEconomy.log.info("NOW HAS:  " + Util.countItem(inventory));
        return new TransactionResult(numRemoved, TransactionResult.ResultType.FAILURE, "withdraw");
    }


    public static TransactionResult deposit(Inventory inventory, int amount){
        if(amount == 0)
            return new TransactionResult(0, TransactionResult.ResultType.SUCCESS, "deposit");

        //ItemEconomy.log.info("Trying to deposit " + amount + " into " + inventory.getType().name() + " with total of " + Util.countItem(inventory));
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
            //ItemEconomy.log.info("Success: Deposited " + numAdded + " into " + inventory.getType().name());
            //ItemEconomy.log.info("NOW HAS:  " + Util.countItem(inventory));
            return new TransactionResult(numAdded, TransactionResult.ResultType.SUCCESS, "deposit");
        }

        //ItemEconomy.log.info("NOW HAS:  " + Util.countItem(inventory));
        return new TransactionResult(numAdded, TransactionResult.ResultType.INSUFFICIENT_SPACE, "deposit");
    }
}