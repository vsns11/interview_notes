package ch05.classes;

// A class is a blueprint: it declares the state (fields) and behaviour (methods) of its objects.
public class BankAccount {
    String owner;          // fields (instance variables): each object gets its own copy
    long balanceCents;     // defaults: null and 0 until assigned

    void deposit(long cents) {
        balanceCents += cents;          // same as this.balanceCents += cents
    }

    boolean withdraw(long cents) {
        if (cents > balanceCents) return false;
        balanceCents -= cents;
        return true;
    }

    String summary() {
        return owner + ": " + balanceCents / 100 + "." + String.format("%02d", balanceCents % 100);
    }
}
