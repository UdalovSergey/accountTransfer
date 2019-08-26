package bank.transaction.model;

public enum TransactionStatus {
    NEW(1),
    IN_PROGRESS(2),
    SUCCESSFUL(3),
    FAILED(4);

    private int id;

    TransactionStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
