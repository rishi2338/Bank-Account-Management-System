package com.rishi.bankweb;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BankController {

    private final BankService bank;

    public BankController(BankService bank) {
        this.bank = bank;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        return session.getAttribute("accountId") == null
                ? "redirect:/login"
                : "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes flash) {

        try {
            String cleanName = fullName == null ? "" : fullName.trim();

            if (cleanName.isBlank()
                    || cleanName.length() > 100
                    || email == null
                    || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                    || password == null
                    || password.length() < 8
                    || password.length() > 72) {
                throw new IllegalArgumentException(
                        "Enter a valid name, email and password (8–72 characters)."
                );
            }

            if (bank.findByEmail(email) != null) {
                throw new IllegalArgumentException(
                        "An account with this email already exists."
                );
            }

            Map<String, Object> account = bank.register(cleanName, email, password);

            // Rotate the session identifier after successful registration.
            request.changeSessionId();
            session.setAttribute(
                    "accountId",
                    ((Number) account.get("id")).longValue()
            );

            flash.addFlashAttribute(
                    "success",
                    "Your account was created successfully."
            );

            return "redirect:/dashboard";

        } catch (Exception e) {
            flash.addFlashAttribute(
                    "error",
                    e.getMessage() == null ? "Unable to create account." : e.getMessage()
            );
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes flash) {

        Map<String, Object> account = bank.authenticate(email, password);

        if (account == null) {
            flash.addFlashAttribute("error", "Incorrect email or password.");
            return "redirect:/login";
        }

        request.changeSessionId();
        session.setAttribute(
                "accountId",
                ((Number) account.get("id")).longValue()
        );

        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long id = (Long) session.getAttribute("accountId");

        if (id == null) {
            return "redirect:/login";
        }

        Map<String, Object> account = bank.account(id);

        if (account == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("account", account);
        model.addAttribute("transactions", bank.history(id));

        return "dashboard";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam BigDecimal amount,
            HttpSession session,
            RedirectAttributes flash) {
        return action(
                session, flash,
                () -> bank.deposit(id(session), amount),
                "Deposit completed."
        );
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam BigDecimal amount,
            HttpSession session,
            RedirectAttributes flash) {
        return action(
                session, flash,
                () -> bank.withdraw(id(session), amount),
                "Withdrawal completed."
        );
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestParam String recipientAccount,
            @RequestParam BigDecimal amount,
            HttpSession session,
            RedirectAttributes flash) {
        return action(
                session, flash,
                () -> bank.transfer(id(session), recipientAccount, amount),
                "Transfer completed."
        );
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private long id(HttpSession session) {
        Long id = (Long) session.getAttribute("accountId");

        if (id == null) {
            throw new IllegalArgumentException("Please sign in again.");
        }

        return id;
    }

    private String action(
            HttpSession session,
            RedirectAttributes flash,
            Runnable operation,
            String successMessage) {

        try {
            operation.run();
            flash.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            flash.addFlashAttribute(
                    "error",
                    e.getMessage() == null ? "Transaction failed." : e.getMessage()
            );
        }

        return "redirect:/dashboard";
    }
}