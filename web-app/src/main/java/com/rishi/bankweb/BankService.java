package com.rishi.bankweb;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankService {

    private static final long ACCOUNT_MIN = 100_000_000_000_000L;
    private static final long ACCOUNT_MAX = 1_000_000_000_000_000L;
    private static final BigDecimal MAX_TRANSACTION = new BigDecimal("10000000");

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public BankService(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    public Map<String, Object> register(String name, String email, String password) {
        String number;

        // Generate a unique, exactly 15-digit account number.
        do {
            number = String.format(
                    Locale.ROOT,
                    "%015d",
                    random.nextLong(ACCOUNT_MIN, ACCOUNT_MAX)
            );
        } while (!jdbc.queryForList(
                "SELECT id FROM bank_accounts WHERE account_number = ?",
                number
        ).isEmpty());

        jdbc.update(
                "INSERT INTO bank_accounts(account_number, full_name, email, password_hash, balance) VALUES(?,?,?,?,?)",
                number,
                name.trim(),
                email.trim().toLowerCase(Locale.ROOT),
                encoder.encode(password),
                new BigDecimal("0.00")
        );

        return findByEmail(email);
    }

    public Map<String, Object> findByEmail(String email) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, account_number, full_name, email, balance, created_at FROM bank_accounts WHERE email = ?",
                email.trim().toLowerCase(Locale.ROOT)
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> authenticate(String email, String password) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM bank_accounts WHERE email = ?",
                email.trim().toLowerCase(Locale.ROOT)
        );

        if (rows.isEmpty()
                || !encoder.matches(password, (String) rows.get(0).get("password_hash"))) {
            return null;
        }

        return publicAccount(rows.get(0));
    }

    public Map<String, Object> account(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, account_number, full_name, email, balance, created_at FROM bank_accounts WHERE id = ?",
                id
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Transactional
    public void deposit(long id, BigDecimal amount) {
        validate(amount);
        jdbc.update(
                "UPDATE bank_accounts SET balance = balance + ? WHERE id = ?",
                amount, id
        );
        record(id, "DEPOSIT", amount, "Cash deposit");
    }

    @Transactional
    public void withdraw(long id, BigDecimal amount) {
        validate(amount);

        int updated = jdbc.update(
                "UPDATE bank_accounts SET balance = balance - ? WHERE id = ? AND balance >= ?",
                amount, id, amount
        );

        if (updated == 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        record(id, "WITHDRAWAL", amount, "Cash withdrawal");
    }

    @Transactional
    public void transfer(long from, String recipient, BigDecimal amount) {
        validate(amount);

        if (recipient == null || !recipient.matches("[0-9]{15}")) {
            throw new IllegalArgumentException(
                    "Recipient account number must contain exactly 15 digits."
            );
        }

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id FROM bank_accounts WHERE account_number = ?",
                recipient
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Recipient account was not found.");
        }

        long to = ((Number) rows.get(0).get("id")).longValue();

        if (to == from) {
            throw new IllegalArgumentException("You cannot transfer to your own account.");
        }

        int debited = jdbc.update(
                "UPDATE bank_accounts SET balance = balance - ? WHERE id = ? AND balance >= ?",
                amount, from, amount
        );

        if (debited == 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        jdbc.update(
                "UPDATE bank_accounts SET balance = balance + ? WHERE id = ?",
                amount, to
        );

        record(from, "TRANSFER", amount, "Transfer to " + recipient);
        record(to, "RECEIVED", amount, "Transfer received");
    }

    public List<Map<String, Object>> history(long id) {
        return jdbc.queryForList(
                "SELECT transaction_type, amount, description, created_at " +
                "FROM bank_transactions WHERE account_id = ? " +
                "ORDER BY created_at DESC, id DESC LIMIT 100",
                id
        );
    }

    private void record(long id, String type, BigDecimal amount, String description) {
        jdbc.update(
                "INSERT INTO bank_transactions(account_id, transaction_type, amount, description) VALUES(?,?,?,?)",
                id, type, amount, description
        );
    }

    private void validate(BigDecimal amount) {
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2
                || amount.compareTo(MAX_TRANSACTION) > 0) {
            throw new IllegalArgumentException(
                    "Enter a valid positive amount (maximum ₹1 crore, up to 2 decimal places)."
            );
        }
    }

    private Map<String, Object> publicAccount(Map<String, Object> row) {
        return Map.of(
                "id", row.get("id"),
                "account_number", row.get("account_number"),
                "full_name", row.get("full_name"),
                "email", row.get("email"),
                "balance", row.get("balance"),
                "created_at", row.get("created_at")
        );
    }
}