package ch08.multicatch;

import java.util.List;

public class MultiCatch {

    static void risky(int which) throws java.io.IOException {
        switch (which) {
            case 0 -> Integer.parseInt("x");
            case 1 -> List.of(1, 2).get(5);
            case 2 -> throw new java.io.IOException("disk full");
            default -> System.out.println("  no problem");
        }
    }

    // Precise rethrow (Java 7): catch broadly, rethrow, and still declare only the specific checked types
    static void rethrow(boolean io) throws java.io.IOException, java.util.concurrent.TimeoutException {
        try {
            if (io) throw new java.io.IOException("io failed");
            throw new java.util.concurrent.TimeoutException("too slow");
        } catch (Exception e) {
            System.out.println("log and rethrow: " + e);
            throw e;                    // compiler knows e is IOException | TimeoutException
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i <= 3; i++) {
            try {
                risky(i);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {     // multi-catch (Java 7)
                System.out.println(i + ": bad input -> " + e.getClass().getSimpleName());
            } catch (java.io.IOException e) {
                System.out.println(i + ": I/O -> " + e.getMessage());
            } finally {
                System.out.println(i + ": finally");
            }
        }

        // Catch order matters: subclass before superclass.
        try {
            Object o = "text";
            Integer n = (Integer) o;
            System.out.println(n);
        } catch (ClassCastException e) {          // specific
            System.out.println("ClassCastException first");
        } catch (RuntimeException e) {            // general
            System.out.println("RuntimeException");
        }
        // Reversing the two catch blocks does not compile:
        // "exception ClassCastException has already been caught"

        // Unnamed catch parameter (Java 22, JEP 456) when you don't use the exception
        String input = "12a";
        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException _) {
            value = -1;
        }
        System.out.println("value = " + value);

        try {
            rethrow(false);
        } catch (java.io.IOException | java.util.concurrent.TimeoutException e) {
            System.out.println("caller got " + e.getClass().getSimpleName());
        }
    }
}
