package ch17.v25;

/** JEP 513 (Java 25): statements before super(...) / this(...) in a constructor. */
public class FlexibleConstructors {
    static class Account {
        final String id;
        Account(String id) { this.id = id; describe(); }
        void describe() { System.out.println("  Account(" + id + ")"); }
    }

    static class SavingsAccount extends Account {
        final double rate;

        SavingsAccount(String id, double rate) {
            if (rate < 0 || rate > 0.2)                                // validate BEFORE the superclass runs
                throw new IllegalArgumentException("bad rate " + rate);
            this.rate = rate;                                          // allowed: initialise own field early
            super(id.strip().toUpperCase());                           // prepare the argument, then call super
        }

        @Override void describe() {                                   // called from Account's constructor:
            System.out.println("  SavingsAccount(" + id + ", rate=" + rate + ")");   // rate is already set
        }
    }

    public static void main(String[] args) {
        new SavingsAccount("  sa-1 ", 0.03);
        try {
            new SavingsAccount("sa-2", 5);
        } catch (IllegalArgumentException e) {
            System.out.println("  rejected before any superclass work: " + e.getMessage());
        }
    }
}
