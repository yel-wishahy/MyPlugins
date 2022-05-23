package shallowcraft.itemeconomy.BankVault;

public enum VaultType {
    REGULAR(1),
    DEPOSIT_ONLY(2),
    WITHDRAW_ONLY(3),
    ALL(4);

    private int id;

    VaultType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static VaultType fromID(int id) {
        return switch (id) {
            case 2 -> DEPOSIT_ONLY;
            case 3 -> WITHDRAW_ONLY;
            case 4 -> ALL;
            default -> REGULAR;
        };
    }
}
