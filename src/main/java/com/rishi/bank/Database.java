package com.rishi.bank;

import java.sql.*;

public final class Database {
    private static final String URL = "jdbc:sqlite:bank.db";
    private Database() {}

    public static Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement s = c.createStatement()) { s.execute("PRAGMA foreign_keys = ON"); }
        return c;
    }

    public static void initialize() {
        String accounts = "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_no TEXT PRIMARY KEY, holder_name TEXT NOT NULL, pin_hash TEXT NOT NULL, " +
                "balance REAL NOT NULL DEFAULT 0 CHECK(balance >= 0), created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)";
        String transactions = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, account_no TEXT NOT NULL, type TEXT NOT NULL, " +
                "amount REAL NOT NULL CHECK(amount > 0), note TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(account_no) REFERENCES accounts(account_no))";
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(accounts); s.execute(transactions);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize database: " + e.getMessage(), e);
        }
    }
}
