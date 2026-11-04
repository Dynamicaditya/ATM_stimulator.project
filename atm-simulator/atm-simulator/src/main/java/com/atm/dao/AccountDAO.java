package com.atm.dao;

import com.atm.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AccountDAO {

    private static final String SELECT =
            "SELECT a.account_number, a.user_id, a.balance, a.pin_hash, a.pin_salt, a.failed_attempts, "
          + "a.locked, a.created_at, u.full_name, u.email, u.phone "
          + "FROM accounts a JOIN users u ON u.id = a.user_id WHERE a.account_number = ?";

    public Account findByNumber(Connection con, String accountNumber) throws SQLException {
        return find(con, SELECT, accountNumber);
    }

    /** Locks the account row until the surrounding transaction ends. */
    public Account findByNumberForUpdate(Connection con, String accountNumber) throws SQLException {
        return find(con, SELECT + " FOR UPDATE", accountNumber);
    }

    private Account find(Connection con, String sql, String accountNumber) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean exists(Connection con, String accountNumber) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Connection con, String accountNumber, long userId, String pinHash,
                       String pinSalt, BigDecimal balance) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, user_id, pin_hash, pin_salt, balance) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setLong(2, userId);
            ps.setString(3, pinHash);
            ps.setString(4, pinSalt);
            ps.setBigDecimal(5, balance);
            ps.executeUpdate();
        }
    }

    public void updateBalance(Connection con, String accountNumber, BigDecimal newBalance) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {
            ps.setBigDecimal(1, newBalance);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        }
    }

    public void updatePin(Connection con, String accountNumber, String pinHash, String pinSalt) throws SQLException {
        String sql = "UPDATE accounts SET pin_hash = ?, pin_salt = ?, failed_attempts = 0 WHERE account_number = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pinHash);
            ps.setString(2, pinSalt);
            ps.setString(3, accountNumber);
            ps.executeUpdate();
        }
    }

    public void updateFailedAttempts(Connection con, String accountNumber, int attempts, boolean locked)
            throws SQLException {
        String sql = "UPDATE accounts SET failed_attempts = ?, locked = ? WHERE account_number = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, attempts);
            ps.setBoolean(2, locked);
            ps.setString(3, accountNumber);
            ps.executeUpdate();
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountNumber(rs.getString("account_number"));
        a.setUserId(rs.getLong("user_id"));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setPinHash(rs.getString("pin_hash"));
        a.setPinSalt(rs.getString("pin_salt"));
        a.setFailedAttempts(rs.getInt("failed_attempts"));
        a.setLocked(rs.getBoolean("locked"));
        Timestamp created = rs.getTimestamp("created_at");
        a.setCreatedAt(created == null ? null : created.toLocalDateTime());
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setPhone(rs.getString("phone"));
        return a;
    }
}
