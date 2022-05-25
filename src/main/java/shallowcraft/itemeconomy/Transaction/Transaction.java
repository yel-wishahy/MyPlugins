package shallowcraft.itemeconomy.Transaction;

import org.bukkit.Bukkit;
import shallowcraft.itemeconomy.Accounts.Account;

import java.util.List;

public class Transaction {

    /**
     * Amount modified by calling method
     */
    public final int amount;

    /**
     * Success or failure of call. Using Enum of ResponseType to determine valid
     * outcomes
     */
    public final ResultType resultType;
    /**
     * Error message if the variable 'type' is ResponseType.FAILURE
     */
    public final String errorMessage;


    public final Account account;
    public final TransactionType transactionType;

    public Transaction(int amount, ResultType resultType, String errorMessage){
        this.amount = amount;
        this.resultType = resultType;
        this.errorMessage = errorMessage;

        //bad practice, will refactor other code and fix later
        this.account = null;
        this.transactionType = null;

        TransactionEvent thisEvent = new TransactionEvent(this);
        Bukkit.getPluginManager().callEvent(thisEvent);

    }
    public Transaction(int amount, ResultType resultType, String errorMessage,Account account, TransactionType transactionType) {
        this.amount = amount;
        this.resultType = resultType;
        this.errorMessage = errorMessage;
        this.account = account;
        this.transactionType = transactionType;

        TransactionEvent thisEvent = new TransactionEvent(this);
        Bukkit.getPluginManager().callEvent(thisEvent);
    }

    @Override
    public String toString(){
        return "Type: " + resultType.toString() + " Amount: " + amount + "Error Msg: " + errorMessage;
    }


    /**
     * Enum for types of Responses indicating the status of a method call.
     */
    public enum ResultType {
        SUCCESS(1),
        FAILURE(2),
        INSUFFICIENT_SPACE(3),
        INSUFFICIENT_FUNDS(4),
        NOT_IMPLEMENTED(5);

        public final int id;
        ResultType(int id) {
            this.id = id;
        }

        public static List<ResultType> failureModes = List.of(FAILURE, INSUFFICIENT_SPACE, INSUFFICIENT_FUNDS);
    }

    public enum TransactionType{
        DEPOSIT(1),
        WITHDRAW(2),
        TAXDEPOSIT(3);

        public final int id;
        TransactionType(int id){this.id = id;}

    }
}
