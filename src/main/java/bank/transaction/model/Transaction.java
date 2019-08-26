package bank.transaction.model;

import java.math.BigDecimal;

public class Transaction {

    private long id;
    private long accountFromId;
    private long accountToId;
    private BigDecimal amount;
    private TransactionStatus status;

    public Transaction(Transaction transaction) {
        this(transaction.getId(), transaction.getAccountFromId(), transaction.getAccountToId(), transaction.getAmount(), transaction.getStatus());
    }

    public Transaction(long accountFromId, long accountToId, BigDecimal amount) {
        this(-1, accountFromId, accountToId, amount, null);
    }

    public Transaction(long accountFromId, long accountToId, BigDecimal amount, TransactionStatus status) {
        this(-1, accountFromId, accountToId, amount, status);
    }

    public Transaction(long id, long accountFromId, long accountToId, BigDecimal amount, TransactionStatus status) {
        this.id = id;
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAccountFromId() {
        return accountFromId;
    }

    public long getAccountToId() {
        return accountToId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
