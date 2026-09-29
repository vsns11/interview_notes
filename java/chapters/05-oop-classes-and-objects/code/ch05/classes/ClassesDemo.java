package ch05.classes;

public class ClassesDemo {
    public static void main(String[] args) {
        BankAccount a = new BankAccount();      // 'new' allocates an object and returns a reference
        a.owner = "Ada";
        a.deposit(10_050);
        BankAccount b = new BankAccount();      // a second, independent object
        b.owner = "Linus";
        b.deposit(2_000);
        System.out.println(a.summary() + " | " + b.summary());
        System.out.println("withdraw 50.00 from Linus: " + b.withdraw(5_000) + " -> " + b.summary());

        BankAccount c = a;                      // copies the reference, not the object
        c.deposit(1);
        System.out.println("via alias: " + a.summary() + ", same object? " + (a == c));

        BankAccount fresh = new BankAccount();
        System.out.println("defaults: owner=" + fresh.owner + ", balance=" + fresh.balanceCents);

        BankAccount nobody = null;              // a reference that points to no object
        try {
            nobody.deposit(1);
        } catch (NullPointerException e) {
            System.out.println("NPE: " + e.getMessage());
        }
    }
}
