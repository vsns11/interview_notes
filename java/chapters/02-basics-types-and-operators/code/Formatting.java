package ch02.io;

import java.util.Locale;

/** print, println, printf / String.format / formatted, System.err. */
public class Formatting {
    public static void main(String[] args) {
        System.out.print("print has no newline; ");
        System.out.println("println adds one.");
        String item = "Coffee";
        int qty = 3;
        double price = 4.5;
        System.out.printf("%-10s|%5d|%8.2f|%n", item, qty, price);
        System.out.printf("%-10s|%05d|%8.3e|%n", "Padded", 42, 12345.678);
        System.out.printf("%s %b %c %x %o %%%n", "str", true, 'J', 255, 8);
        System.out.printf(Locale.GERMANY, "German: %,.2f%n", 1234567.891);
        System.out.printf(Locale.US, "US:     %,.2f%n", 1234567.891);
        String line = String.format("Total: %.2f", qty * price);
        String line2 = "Items: %d".formatted(qty);         // Java 15
        System.out.println(line + " / " + line2);
        System.err.println("(this line goes to standard error)");
    }
}
