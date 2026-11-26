package com.rishi.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class BankUI extends JFrame {
    private final BankService service = new BankService();
    private final Color navy = new Color(22, 45, 78);
    private final Color blue = new Color(40, 105, 180);
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private BankService.Account current;

    public BankUI() {
        setTitle("Bank Account Management System");
        setSize(850, 570); setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE); showWelcome();
    }

    private JPanel page(String title, String subtitle) {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(26, 32, 28, 32)); root.setBackground(new Color(245, 248, 252));
        JPanel head = new JPanel(); head.setOpaque(false); head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 26)); t.setForeground(navy);
        JLabel s = new JLabel(subtitle); s.setFont(new Font("Segoe UI", Font.PLAIN, 13)); s.setForeground(new Color(100, 115, 135));
        head.add(t); head.add(Box.createVerticalStrut(6)); head.add(s); root.add(head, BorderLayout.NORTH);
        return root;
    }

    private JButton button(String text) {
        JButton b = new JButton(text); b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE); b.setBackground(blue); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 18, 11, 18)); return b;
    }
    private JTextField field(int columns) {
        JTextField f = new JTextField(columns); f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setPreferredSize(new Dimension(260, 36)); return f;
    }

    private void showWelcome() {
        JPanel root = page("Welcome to SimpleBank", "A simple way to manage your bank account");
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE); card.setBorder(new EmptyBorder(28, 34, 28, 34));
        JLabel logo = new JLabel("SIMPLEBANK"); logo.setFont(new Font("Segoe UI", Font.BOLD, 32)); logo.setForeground(blue); logo.setAlignmentX(.5f);
        JLabel desc = new JLabel("Banking made simple"); desc.setFont(new Font("Segoe UI", Font.PLAIN, 17)); desc.setAlignmentX(.5f);
        JButton login = button("Login to Account"); login.setAlignmentX(.5f);
        JButton create = button("Create New Account"); create.setAlignmentX(.5f);
        login.addActionListener(e -> showLogin()); create.addActionListener(e -> showCreate());
        card.add(logo); card.add(Box.createVerticalStrut(10)); card.add(desc); card.add(Box.createVerticalStrut(28));
        card.add(login); card.add(Box.createVerticalStrut(12)); card.add(create);
        center.add(card); root.add(center, BorderLayout.CENTER); setContentPane(root); revalidate(); repaint();
    }

    private void showLogin() {
        JPanel root = page("Account Login", "Enter your account number and 4-digit PIN");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        JTextField account = field(22); JPasswordField pin = new JPasswordField(22); pin.setPreferredSize(new Dimension(260, 36));
        addField(form, "Account number", account, 0); addField(form, "4-digit PIN", pin, 1);
        JButton login = button("Login"); JButton back = new JButton("Back");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(login); actions.add(back);
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false); body.add(form, BorderLayout.CENTER); body.add(actions, BorderLayout.SOUTH);
        root.add(body, BorderLayout.CENTER);
        login.addActionListener(e -> {
            try {
                current = service.login(account.getText(), new String(pin.getPassword()));
                if (current == null) message("Invalid account number or PIN.", JOptionPane.ERROR_MESSAGE); else showDashboard();
            } catch (Exception ex) { error(ex); }
        });
        back.addActionListener(e -> showWelcome()); setContentPane(root); revalidate(); repaint();
    }

    private void showCreate() {
        JPanel root = page("Create Bank Account", "Fill in the details to open a new account");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        JTextField name = field(22); JPasswordField pin = new JPasswordField(22); pin.setPreferredSize(new Dimension(260, 36));
        JTextField opening = field(22); opening.setText("0");
        addField(form, "Account holder name", name, 0); addField(form, "Create 4-digit PIN", pin, 1);
        addField(form, "Opening deposit (INR)", opening, 2);
        JButton create = button("Create Account"); JButton back = new JButton("Back");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(create); actions.add(back);
        JPanel body = new JPanel(new BorderLayout()); body.setOpaque(false); body.add(form, BorderLayout.CENTER); body.add(actions, BorderLayout.SOUTH);
        root.add(body, BorderLayout.CENTER);
        create.addActionListener(e -> {
            try {
                BankService.Account a = service.createAccount(name.getText(), new String(pin.getPassword()), Double.parseDouble(opening.getText().trim()));
                JOptionPane.showMessageDialog(this, "Account created successfully!\nAccount number: " + a.number()
                        + "\nPlease save this number for login.", "Account Created", JOptionPane.INFORMATION_MESSAGE);
                showLogin();
            } catch (Exception ex) { error(ex); }
        });
        back.addActionListener(e -> showWelcome()); setContentPane(root); revalidate(); repaint();
    }

    private void addField(JPanel panel, String label, JComponent input, int row) {
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(10, 12, 10, 12); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = row; JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); panel.add(l, g);
        g.gridx = 1; panel.add(input, g);
    }

    private void showDashboard() {
        JPanel root = page("Hello, " + current.holder(), "Account number: " + current.number());
        JPanel content = new JPanel(new GridLayout(2, 2, 16, 16)); content.setOpaque(false);
        content.add(actionCard("Deposit Money", "Add money to your account", () -> amountAction(true)));
        content.add(actionCard("Withdraw Money", "Withdraw from available balance", () -> amountAction(false)));
        content.add(actionCard("Check Balance", "View your current account balance", this::showBalance));
        content.add(actionCard("Transaction History", "View deposits and withdrawals", this::showHistory));
        JButton logout = new JButton("Logout"); logout.addActionListener(e -> { current = null; showWelcome(); });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setOpaque(false); bottom.add(logout);
        root.add(content, BorderLayout.CENTER); root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root); revalidate(); repaint();
    }

    private JPanel actionCard(String title, String subtitle, Runnable action) {
        JPanel p = new JPanel(); p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(22, 22, 22, 22));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 18)); t.setForeground(navy);
        JLabel s = new JLabel(subtitle); s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton b = button("Open"); b.setAlignmentX(Component.LEFT_ALIGNMENT); b.addActionListener(e -> action.run());
        p.add(t); p.add(Box.createVerticalStrut(8)); p.add(s); p.add(Box.createVerticalGlue()); p.add(b); return p;
    }

    private void amountAction(boolean deposit) {
        String input = JOptionPane.showInputDialog(this, "Enter amount:", deposit ? "Deposit" : "Withdrawal", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            double amount = Double.parseDouble(input.trim());
            if (deposit) service.deposit(current.number(), amount); else service.withdraw(current.number(), amount);
            JOptionPane.showMessageDialog(this, (deposit ? "Deposit" : "Withdrawal") + " successful.\nNew balance: " + money.format(service.balance(current.number())));
        } catch (Exception ex) { error(ex); }
    }
    private void showBalance() {
        try { JOptionPane.showMessageDialog(this, "Available balance: " + money.format(service.balance(current.number())), "Account Balance", JOptionPane.INFORMATION_MESSAGE); }
        catch (Exception ex) { error(ex); }
    }
    private void showHistory() {
        try {
            var rows = service.history(current.number());
            String[] columns = {"Date", "Type", "Amount", "Note"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (var t : rows) model.addRow(new Object[]{t.date(), t.type(), money.format(t.amount()), t.note()});
            JTable table = new JTable(model); table.setRowHeight(25); table.setFillsViewportHeight(true);
            JScrollPane scroll = new JScrollPane(table); scroll.setPreferredSize(new Dimension(680, 300));
            JOptionPane.showMessageDialog(this, scroll, "Transaction History", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) { error(ex); }
    }
    private void error(Exception ex) {
        String msg = ex.getMessage() == null ? "An unexpected error occurred." : ex.getMessage();
        if (ex instanceof NumberFormatException) msg = "Enter a valid numeric amount.";
        message(msg, JOptionPane.ERROR_MESSAGE);
    }
    private void message(String msg, int type) { JOptionPane.showMessageDialog(this, msg, "Bank Account Management", type); }
}
