package ch08.custom;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;

public class CustomExceptions {

    // Checked: the caller is expected to recover (e.g. show "insufficient funds" to the user)
    static class InsufficientFundsException extends Exception {
        private final BigDecimal shortfall;
        InsufficientFundsException(BigDecimal shortfall) {
            super("insufficient funds: short by " + shortfall);
            this.shortfall = shortfall;
        }
        BigDecimal getShortfall() { return shortfall; }
    }

    // Unchecked: a programming/state error in our domain
    static class AccountFrozenException extends RuntimeException {
        AccountFrozenException(String accountId) { super("account " + accountId + " is frozen"); }
    }

    // A higher-level exception that wraps (chains) a lower-level cause
    static class ReportException extends Exception {
        ReportException(String message, Throwable cause) { super(message, cause); }
    }

    static class Account {
        private final String id;
        private BigDecimal balance;
        private boolean frozen;
        Account(String id, String balance) { this.id = id; this.balance = new BigDecimal(balance); }

        void withdraw(BigDecimal amount) throws InsufficientFundsException {
            if (amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive: " + amount);
            if (frozen) throw new AccountFrozenException(id);
            if (amount.compareTo(balance) > 0) throw new InsufficientFundsException(amount.subtract(balance));
            balance = balance.subtract(amount);
        }
    }

    static String loadTemplate(String name) throws IOException {
        throw new IOException("template '" + name + "' not found on disk");
    }

    static String buildReport(String name) throws ReportException {
        try {
            return loadTemplate(name);
        } catch (IOException e) {
            throw new ReportException("could not build report " + name, e);   // translate + chain
        }
    }

    public static void main(String[] args) {
        Account acc = new Account("ACC-1", "100.00");
        String[] attempts = {"30.00", "500.00", "-1"};
        for (String amt : attempts) {
            try {
                acc.withdraw(new BigDecimal(amt));
                System.out.println("withdrew " + amt + ", balance " + acc.balance);
            } catch (InsufficientFundsException e) {
                System.out.println(e.getMessage() + " (shortfall field = " + e.getShortfall() + ")");
            } catch (IllegalArgumentException e) {
                System.out.println("rejected: " + e.getMessage());
            }
        }
        acc.frozen = true;
        try {
            acc.withdraw(new BigDecimal("1"));
        } catch (InsufficientFundsException | AccountFrozenException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        try {
            buildReport("monthly");
        } catch (ReportException e) {
            System.out.println("error: " + e.getMessage());
            System.out.println("cause: " + e.getCause());
        }

        // Wrapping a checked exception in an unchecked one (e.g. inside a lambda)
        try {
            java.util.List.of("a").forEach(n -> {
                try {
                    loadTemplate(n);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            System.out.println("unwrapped cause: " + e.getCause().getMessage());
        }
    }
}
