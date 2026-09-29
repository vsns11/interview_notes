package lld.ch06.errors;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Error handling: exceptions not codes, unchecked with context, wrap third-party errors, special case, no null. */
@SuppressWarnings("serial") // the nested exception classes are never serialized
public class Main {

    // ---------------- BEFORE ----------------
    static class Before {
        static final int OK = 0, E_NO_ACCOUNT = 1, E_NO_MONEY = 2;
        static final Map<String, Long> BALANCES = new HashMap<>(Map.of("A", 500L, "B", 100L));
        static final Map<String, Integer> POINTS = new HashMap<>(Map.of("A", 40));

        static int transfer(String from, String to, long amount) {        // caller must check an int
            Long f = BALANCES.get(from), t = BALANCES.get(to);
            if (f == null || t == null) return E_NO_ACCOUNT;               // which one? no context
            if (f < amount) return E_NO_MONEY;                             // how much was missing?
            BALANCES.put(from, f - amount); BALANCES.put(to, t + amount);
            return OK;
        }
        static List<String> history(String id) { return null; }          // "no history" = null
        static Integer loyaltyPoints(String id) { return POINTS.get(id); } // null for non-members
    }

    // ---------------- AFTER ----------------
    static class After {
        /** Unchecked, domain-specific, with context. Defined by what the CALLER needs to handle. */
        static class BankingException extends RuntimeException {
            BankingException(String msg) { super(msg); }
            BankingException(String msg, Throwable cause) { super(msg, cause); }
        }
        static final class AccountNotFoundException extends BankingException {
            AccountNotFoundException(String id) { super("account '" + id + "' does not exist"); }
        }
        static final class InsufficientFundsException extends BankingException {
            InsufficientFundsException(String id, long requested, long available) {
                super("account '" + id + "' has " + available + " but " + requested + " was requested");
            }
        }

        /** SPECIAL CASE pattern: an object that represents "no loyalty membership" -- callers need no null check. */
        interface Loyalty { int points(); String tier(); }
        record Member(int points) implements Loyalty { public String tier() { return points >= 100 ? "GOLD" : "SILVER"; } }
        enum NoMembership implements Loyalty { INSTANCE; public int points() { return 0; } public String tier() { return "NONE"; } }

        /** Third-party library that throws its own checked exceptions (we can't change it). */
        static final class VendorSmsClient {
            void push(String phone, String text) throws IOException {
                if (phone.startsWith("000")) throw new IOException("socket timeout to sms-gw-3");
            }
        }
        /** Wrapper: one exception type our code understands; original kept as the cause. */
        static final class SmsNotifier {
            private final VendorSmsClient client = new VendorSmsClient();
            void send(String phone, String text) {
                try { client.push(phone, text); }
                catch (IOException e) { throw new BankingException("could not send SMS to " + phone, e); }
            }
        }

        static final class Bank {
            private final Map<String, Long> balances = new HashMap<>(Map.of("A", 500L, "B", 100L));
            private final Map<String, Integer> points = new HashMap<>(Map.of("A", 40));

            void transfer(String from, String to, long amount) {
                Objects.requireNonNull(from, "from"); Objects.requireNonNull(to, "to");     // don't accept null
                long available = balanceOf(from);
                balanceOf(to);                                                              // validates 'to' exists
                if (available < amount) throw new InsufficientFundsException(from, amount, available);
                balances.merge(from, -amount, Long::sum);
                balances.merge(to, amount, Long::sum);
            }
            long balanceOf(String id) {
                Long b = balances.get(id);
                if (b == null) throw new AccountNotFoundException(id);
                return b;
            }
            List<String> history(String id) { return List.of(); }                          // empty, never null
            Optional<Long> findBalance(String id) { return Optional.ofNullable(balances.get(id)); }
            Loyalty loyaltyOf(String id) {
                Integer p = points.get(id);
                return p == null ? NoMembership.INSTANCE : new Member(p);
            }
        }

        /** "Write the try first": a resource that is always released, via try-with-resources. */
        static final class AuditFile implements AutoCloseable {
            AuditFile() { System.out.println("  audit file opened"); }
            void write(String line) { System.out.println("  audit: " + line); }
            @Override public void close() { System.out.println("  audit file closed"); }
        }
    }

    public static void main(String[] args) {
        System.out.println("== BEFORE ==");
        int code = Before.transfer("A", "Z", 50);
        System.out.println("transfer returned " + code + " (which account? caller has to decode)");
        try {
            System.out.println("history size " + Before.history("A").size());
        } catch (NullPointerException e) { System.out.println("history(): NullPointerException -- someone forgot the null check"); }
        try {
            int pts = Before.loyaltyPoints("B");            // auto-unboxing a null Integer
            System.out.println(pts);
        } catch (NullPointerException e) { System.out.println("loyaltyPoints(B): NullPointerException on unboxing"); }

        System.out.println("== AFTER ==");
        var bank = new After.Bank();
        for (String[] t : new String[][]{{"A", "Z", "50"}, {"B", "A", "900"}, {"A", "B", "200"}}) {
            try {
                bank.transfer(t[0], t[1], Long.parseLong(t[2]));
                System.out.println("transfer " + t[0] + "->" + t[1] + " of " + t[2] + " ok; A=" + bank.balanceOf("A") + " B=" + bank.balanceOf("B"));
            } catch (After.BankingException e) {
                System.out.println("transfer failed: " + e.getMessage());
            }
        }
        System.out.println("history(A) size = " + bank.history("A").size());
        System.out.println("findBalance(Z) = " + bank.findBalance("Z"));
        for (String id : List.of("A", "B")) {
            var l = bank.loyaltyOf(id);                    // no null check needed
            System.out.println("loyalty " + id + ": " + l.points() + " pts, tier " + l.tier());
        }
        try {
            new After.SmsNotifier().send("000123", "hi");
        } catch (After.BankingException e) {
            System.out.println(e.getMessage() + " (cause: " + e.getCause().getMessage() + ")");
        }
        try (var audit = new After.AuditFile()) {
            audit.write("transfer A->B 200");
            throw new IllegalStateException("simulated crash");
        } catch (IllegalStateException e) {
            System.out.println("  caught: " + e.getMessage() + " -- file was still closed first");
        }
    }
}
