package com.atm.dao;

import com.atm.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    /** Inserts the user and returns the generated id. */
    public long insert(Connection con, User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for user insert.");
                }
                long id = keys.getLong(1);
                user.setId(id);
                return id;
            }
        }
    }

    public boolean emailExists(Connection con, String email) throws SQLException {
        return exists(con, "SELECT 1 FROM users WHERE email = ?", email);
    }

    public boolean phoneExists(Connection con, String phone) throws SQLException {
        return exists(con, "SELECT 1 FROM users WHERE phone = ?", phone);
    }

    private boolean exists(Connection con, String sql, String value) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
