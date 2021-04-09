package shallowcraft.itemeconomy.Transaction;

import java.util.List;

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
