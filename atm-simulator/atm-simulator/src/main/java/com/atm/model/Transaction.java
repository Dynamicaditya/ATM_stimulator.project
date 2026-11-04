package com.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private long id;
    private final String accountNumber;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String relatedAccount;
    private final String description;
    private LocalDateTime createdAt;

    public Transaction(String accountNumber, TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String relatedAccount, String description) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedAccount = relatedAccount;
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getRelatedAccount() { return relatedAccount; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
