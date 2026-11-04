package com.atm.config;

import com.atm.exception.ATMException;
import com.atm.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Central place for JDBC connections and a small transaction helper. */
public final class DatabaseConfig {

    /** A unit of work that runs inside one database connection. */
    @FunctionalInterface
    public interface TxWork<T> {
        T run(Connection connection) throws SQLException, ATMException;
    }

    private static final String DB_DOWN_MESSAGE =
            "Database error. Please make sure MySQL is running and db.properties is correct.";

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            System.err.println("Could not read db.properties: " + e.getMessage());
        }
    }

    private DatabaseConfig() { }

    private static String setting(String envName, String propName, String fallback) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return PROPS.getProperty(propName, fallback);
    }

    public static Connection getConnection() throws SQLException {
        String url = setting("ATM_DB_URL", "db.url", "jdbc:mysql://localhost:3306/atm_db");
        String user = setting("ATM_DB_USER", "db.user", "root");
        String pass = setting("ATM_DB_PASSWORD", "db.password", "");
        return DriverManager.getConnection(url, user, pass);
    }

    /** Runs work in a single transaction: commit on success, rollback on any failure. */
    public static <T> T inTransaction(TxWork<T> work) throws ATMException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                T result = work.run(con);
                con.commit();
                return result;
            } catch (SQLException | ATMException | RuntimeException e) {
                safeRollback(con);
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException(DB_DOWN_MESSAGE, e);
        }
    }

    /** Read-only helper (no explicit transaction). */
    public static <T> T query(TxWork<T> work) throws ATMException {
        try (Connection con = getConnection()) {
            return work.run(con);
        } catch (SQLException e) {
            throw new DatabaseException(DB_DOWN_MESSAGE, e);
        }
    }

    private static void safeRollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ignored) {
            // nothing else we can do
        }
    }
}
