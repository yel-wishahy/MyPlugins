package shallowcraft.itemeconomy.Transaction;

import java.util.List;

public class TransactionResult {

    /**
     * Amount modified by calling method
     */
    public final int amount;

    /**
     * Success or failure of call. Using Enum of ResponseType to determine valid
     * outcomes
     */
    public final ResultType type;
    /**
     * Error message if the variable 'type' is ResponseType.FAILURE
     */
    public final String errorMessage;

    /**
     * Constructor for EconomyResponse
     * @param amount Amount modified during operation
     * @param type Success or failure type of the operation
     * @param errorMessage Error message if necessary (commonly null)
     */
    public TransactionResult(int amount, ResultType type, String errorMessage) {
        this.amount = amount;
        this.type = type;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString(){
        return "Type: " + type.toString() + " Amount: " + amount + "Error Msg: " + errorMessage;
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

        private int id;

        ResultType(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }

        public static List<ResultType> failureModes = List.of(FAILURE, INSUFFICIENT_SPACE, INSUFFICIENT_FUNDS);
    }
}
