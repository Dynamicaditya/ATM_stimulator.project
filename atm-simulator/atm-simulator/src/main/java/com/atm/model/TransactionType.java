package com.atm.model;

public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_OUT("Transfer Sent"),
    TRANSFER_IN("Transfer Received");

    private final String label;

    TransactionType(String label) { this.label = label; }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
