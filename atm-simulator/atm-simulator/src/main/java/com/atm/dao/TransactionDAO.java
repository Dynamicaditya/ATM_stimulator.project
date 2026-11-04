package com.atm.dao;

import com.atm.model.Transaction;
import com.atm.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insert(Connection con, Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions "
                   + "(account_number, type, amount, balance_after, related_account, description) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getAccountNumber());
            ps.setString(2, t.getType().name());
            ps.setBigDecimal(3, t.getAmount());
            ps.setBigDecimal(4, t.getBalanceAfter());
            ps.setString(5, t.getRelatedAccount());
            ps.setString(6, t.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getLong(1));
                }
            }
        }
    }

    /** Most recent transactions first. */
    public List<Transaction> findRecent(Connection con, String accountNumber, int limit) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_number = ? "
                   + "ORDER BY created_at DESC, id DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setInt(2, limit);
            return readAll(ps);
        }
    }

    /**
     * Filtered history. Any of type / from / to may be null (no filter).
     * Both dates are inclusive.
     */
    public List<Transaction> search(Connection con, String accountNumber, TransactionType type,
                                    LocalDate from, LocalDate to, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE account_number = ?");
        if (type != null) {
            sql.append(" AND type = ?");
        }
        if (from != null) {
            sql.append(" AND created_at >= ?");
        }
        if (to != null) {
            sql.append(" AND created_at < ?");
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ?");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, accountNumber);
            if (type != null) {
                ps.setString(i++, type.name());
            }
            if (from != null) {
                ps.setTimestamp(i++, Timestamp.valueOf(from.atStartOfDay()));
            }
            if (to != null) {
                ps.setTimestamp(i++, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            }
            ps.setInt(i, limit);
            return readAll(ps);
        }
    }

    private List<Transaction> readAll(PreparedStatement ps) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getString("account_number"),
                        TransactionType.valueOf(rs.getString("type")),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("balance_after"),
                        rs.getString("related_account"),
                        rs.getString("description"));
                t.setId(rs.getLong("id"));
                Timestamp ts = rs.getTimestamp("created_at");
                t.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
                list.add(t);
            }
        }
        return list;
    }
}
