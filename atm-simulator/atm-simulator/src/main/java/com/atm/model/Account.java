package com.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** An account together with its holder's details (joined view). */
public class Account {
    private String accountNumber;
    private long userId;
    private String fullName;
    private String email;
    private String phone;
    private BigDecimal balance;
    private String pinHash;
    private String pinSalt;
    private int failedAttempts;
    private boolean locked;
    private LocalDateTime createdAt;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public String getPinSalt() { return pinSalt; }
    public void setPinSalt(String pinSalt) { this.pinSalt = pinSalt; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** e.g. "XXXX XXXX 0001" */
    public String getMaskedNumber() {
        String n = accountNumber;
        return "XXXX XXXX " + n.substring(Math.max(0, n.length() - 4));
    }
}
