package ch06.interfaces;

import java.util.ArrayList;
import java.util.List;

public class InterfacesDemo {

    interface Payable {
        double TAX_RATE = 0.2;                        // implicitly public static final

        double amount();                              // implicitly public abstract

        default double amountAfterTax() {             // default method (Java 8): inherited implementation
            return round(amount() * (1 - TAX_RATE));
        }

        static Payable of(double value) {             // static method (Java 8): called as Payable.of(..)
            return () -> value;                       // Payable has one abstract method, so a lambda works
        }

        private static double round(double v) {      // private method (Java 9): helper for defaults
            return Math.round(v * 100) / 100.0;
        }
    }

    interface Printable {
        default String label() { return "printable"; }
    }
    interface Shippable {
        default String label() { return "shippable"; }
    }

    // A class can implement MANY interfaces (but extend only one class).
    static class Invoice implements Payable, Printable, Shippable {
        private final double total;
        Invoice(double total) { this.total = total; }
        @Override public double amount() { return total; }
        @Override public String label() {             // must resolve the conflict between two defaults
            return Printable.super.label() + "+" + Shippable.super.label();
        }
    }

    public static void main(String[] args) {
        List<Payable> bills = new ArrayList<>();
        bills.add(new Invoice(100));
        bills.add(Payable.of(59.99));
        bills.add(new Payable() {                     // anonymous class implementing the interface
            public double amount() { return 10; }
            public double amountAfterTax() { return 10; }   // overrides the default: tax-exempt
        });
        for (Payable p : bills)
            System.out.println(p.amount() + " -> after tax " + p.amountAfterTax());
        System.out.println("label: " + new Invoice(1).label() + ", TAX_RATE = " + Payable.TAX_RATE);
        System.out.println("Invoice is a Printable? " + (bills.get(0) instanceof Printable));
    }
}
