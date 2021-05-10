package shallowcraft.itemeconomy.BankVault;

public enum VaultType {
    REGULAR(1),
    DEPOSIT_ONLY(2),
    WITHDRAW_ONLY(3);

    private int id;

    VaultType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static VaultType fromID(int id) {
        switch (id) {
            case 1:
                return REGULAR;
            case 2:
                return DEPOSIT_ONLY;
            case 3:
                return WITHDRAW_ONLY;
            default:
                return REGULAR;
        }
    }
}
