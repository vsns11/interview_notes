package ch02.numbers;

/** Integer overflow happens silently; Math.*Exact makes it loud. */
public class Overflow {
    public static void main(String[] args) {
        int max = Integer.MAX_VALUE;
        System.out.println("MAX_VALUE + 1 = " + (max + 1));
        System.out.println("Math.abs(Integer.MIN_VALUE) = " + Math.abs(Integer.MIN_VALUE));
        System.out.println("-Integer.MIN_VALUE = " + (-Integer.MIN_VALUE));

        int low = 2_000_000_000, high = 2_100_000_000;
        System.out.println("(low + high) / 2 = " + (low + high) / 2 + "   <- overflow bug");
        System.out.println("low + (high - low) / 2 = " + (low + (high - low) / 2));
        System.out.println("(low + high) >>> 1 = " + ((low + high) >>> 1));

        try {
            Math.addExact(max, 1);
        } catch (ArithmeticException e) {
            System.out.println("Math.addExact(MAX, 1) -> " + e.getMessage());
        }
        try {
            Math.toIntExact(3_000_000_000L);
        } catch (ArithmeticException e) {
            System.out.println("Math.toIntExact(3e9) -> " + e.getMessage());
        }
        long seconds = 30L * 24 * 60 * 60 * 1000;     // L on the first operand: all long math
        System.out.println("30 days in ms (long) = " + seconds + ", as int math = " + (30 * 24 * 60 * 60 * 1000));
    }
}
