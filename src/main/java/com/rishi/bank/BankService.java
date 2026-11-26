package com.rishi.bank;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class BankService {
    public record Account(String number, String holder, double balance) {}
    public record Transaction(String type, double amount, String note, String date) {}
    private final SecureRandom random = new SecureRandom();

    private String hashPin(String pin) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(pin.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public Account createAccount(String name, String pin, double openingDeposit) throws SQLException {
        if (name == null || name.trim().length() < 2) throw new IllegalArgumentException("Enter a valid name.");
        if (pin == null || !pin.matches("\\d{4}")) throw new IllegalArgumentException("PIN must contain exactly 4 digits.");
        if (!Double.isFinite(openingDeposit) || openingDeposit < 0) throw new IllegalArgumentException("Opening deposit cannot be negative.");
        String number;
        try (Connection c = Database.connect()) {
            do { number = String.valueOf(1000000000L + random.nextInt(900000000)); }
            while (accountExists(c, number));
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("INSERT INTO accounts(account_no,holder_name,pin_hash,balance) VALUES(?,?,?,?)")) {
                p.setString(1, number); p.setString(2, name.trim()); p.setString(3, hashPin(pin)); p.setDouble(4, openingDeposit); p.executeUpdate();
            }
            if (openingDeposit > 0) addTransaction(c, number, "DEPOSIT", openingDeposit, "Opening deposit");
            c.commit();
            return new Account(number, name.trim(), openingDeposit);
        }
    }

    private boolean accountExists(Connection c, String number) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM accounts WHERE account_no=?")) {
            p.setString(1, number); try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    public Account login(String number, String pin) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(
                "SELECT account_no,holder_name,balance,pin_hash FROM accounts WHERE account_no=?")) {
            p.setString(1, number.trim());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next() || !MessageDigest.isEqual(r.getString("pin_hash").getBytes(StandardCharsets.UTF_8),
                        hashPin(pin).getBytes(StandardCharsets.UTF_8))) return null;
                return new Account(r.getString("account_no"), r.getString("holder_name"), r.getDouble("balance"));
            }
        }
    }

    public double balance(String number) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement("SELECT balance FROM accounts WHERE account_no=?")) {
            p.setString(1, number); try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Account not found.");
                return r.getDouble(1);
            }
        }
    }

    public void deposit(String number, double amount) throws SQLException {
        validateAmount(amount);
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("UPDATE accounts SET balance=balance+? WHERE account_no=?")) {
                p.setDouble(1, amount); p.setString(2, number);
                if (p.executeUpdate() != 1) throw new IllegalArgumentException("Account not found.");
            }
            addTransaction(c, number, "DEPOSIT", amount, "Cash deposit"); c.commit();
        }
    }

    public void withdraw(String number, double amount) throws SQLException {
        validateAmount(amount);
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement("UPDATE accounts SET balance=balance-? WHERE account_no=? AND balance>=?")) {
                p.setDouble(1, amount); p.setString(2, number); p.setDouble(3, amount);
                if (p.executeUpdate() != 1) throw new IllegalArgumentException("Insufficient balance or account not found.");
            }
            addTransaction(c, number, "WITHDRAWAL", amount, "Cash withdrawal"); c.commit();
        }
    }

    private void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
    }

    private void addTransaction(Connection c, String number, String type, double amount, String note) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO transactions(account_no,type,amount,note) VALUES(?,?,?,?)")) {
            p.setString(1, number); p.setString(2, type); p.setDouble(3, amount); p.setString(4, note); p.executeUpdate();
        }
    }

    public List<Transaction> history(String number) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(
                "SELECT type,amount,note,created_at FROM transactions WHERE account_no=? ORDER BY id DESC")) {
            p.setString(1, number);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(new Transaction(r.getString(1), r.getDouble(2), r.getString(3), r.getString(4)));
            }
        }
        return list;
    }
}
