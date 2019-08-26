package bank.account.model;

import java.math.BigDecimal;

public class Account {

    //TODO: ID should be set only once, without ability to change it. Ony repository should responses of it.
    private long id;
    private String ownerName;
    private BigDecimal amount;
    private BigDecimal blockedAmount;

    public Account(long id, String ownerName, BigDecimal amount, BigDecimal blockedAmount) {
        this.id = id;
        this.ownerName = ownerName;
        this.amount = amount;
        this.blockedAmount = blockedAmount;
    }

    public Account(long id, String ownerName, BigDecimal amount) {
       this(id, ownerName, amount, BigDecimal.ZERO);
    }

    public Account(Account account) {
        this(account.id, account.getOwnerName(), account.amount, account.getBlockedAmount());
    }

    public Account(String ownerName, BigDecimal amount) {
        this(-1, ownerName, amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getBlockedAmount() {
        return blockedAmount;
    }

    public void setBlockedAmount(BigDecimal blockedAmount) {
        this.blockedAmount = blockedAmount;
    }
}
