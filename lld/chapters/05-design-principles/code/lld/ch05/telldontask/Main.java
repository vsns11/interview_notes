package lld.ch05.telldontask;

/** Tell, Don't Ask: tell an object what you want; don't pull its data out, decide, and push data back in. */
public class Main {

    // ---------------- BEFORE: callers ask for state and make the decision themselves ----------------
    static class Before {
        static class Account {
            private long balancePaise;
            private boolean frozen;
            Account(long balance) { this.balancePaise = balance; }
            long getBalance() { return balancePaise; }
            void setBalance(long b) { balancePaise = b; }
            boolean isFrozen() { return frozen; }
            void setFrozen(boolean f) { frozen = f; }
        }
        // ATM remembered the "frozen" rule...
        static void atmWithdraw(Account a, long amt) {
            if (!a.isFrozen() && a.getBalance() >= amt) a.setBalance(a.getBalance() - amt);
        }
        // ...the mobile app forgot it. The business rule is scattered and now inconsistent.
        static void appWithdraw(Account a, long amt) {
            if (a.getBalance() >= amt) a.setBalance(a.getBalance() - amt);
        }
    }

    // ---------------- AFTER: the rule lives with the data it protects ----------------
    static class After {
        static final class Account {
            private long balancePaise;
            private boolean frozen;
            Account(long balance) { this.balancePaise = balance; }
            void freeze() { frozen = true; }
            void withdraw(long amt) {
                if (amt <= 0) throw new IllegalArgumentException("amount must be positive");
                if (frozen) throw new IllegalStateException("account is frozen");
                if (amt > balancePaise) throw new IllegalStateException("insufficient funds");
                balancePaise -= amt;
            }
            long balance() { return balancePaise; }   // queries are fine for display -- just don't decide outside
        }
    }

    public static void main(String[] args) {
        System.out.println("== BEFORE ==");
        var a = new Before.Account(1_000);
        a.setFrozen(true);
        Before.atmWithdraw(a, 300);
        System.out.println("after ATM on frozen account: " + a.getBalance());
        Before.appWithdraw(a, 300);
        System.out.println("after APP on frozen account: " + a.getBalance() + "   <-- rule bypassed");

        System.out.println("== AFTER ==");
        var b = new After.Account(1_000);
        b.withdraw(300);
        System.out.println("withdrew 300, balance " + b.balance());
        b.freeze();
        try { b.withdraw(300); } catch (IllegalStateException e) { System.out.println("rejected: " + e.getMessage()); }
        System.out.println("balance still " + b.balance());
    }
}
