package lld.ch02.classes;

public class Main {
    public static void main(String[] args) {
        Account a = new Account("S-1", "Ada", 50_000);          // 3-arg constructor
        Account b = new Account("C-1", "Kai");                  // 2-arg constructor chains to 3-arg
        b.deposit(1_000).deposit(500);                          // chaining via 'return this'

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("accounts opened (static, shared): " + Account.openedCount());

        Account alias = a;                                      // copies the REFERENCE, not the object
        alias.deposit(1);
        System.out.println("after alias.deposit(1), a = " + a + ", same object? " + (alias == a));

        try {
            new Account("X", "Bad", -5);
        } catch (IllegalArgumentException e) {
            System.out.println("constructor refused: " + e.getMessage());
        }
        System.out.println("accounts opened: " + Account.openedCount() + " (failed construction did not count)");
    }
}
