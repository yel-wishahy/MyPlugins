package shallowcraft.itemeconomy.Transaction;

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

    /**
     * Checks if an operation was successful
     * @return Value
     */
    public boolean transactionSuccess() {
        return type == ResultType.SUCCESS;
    }
}
