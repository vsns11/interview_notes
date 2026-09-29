package lld.ch06.testing;

/** Runs all test classes with the MiniTest runner and prints a summary. */
public class Main {
    public static void main(String[] args) {
        MiniTest.Summary total = new MiniTest.Summary(0, 0);
        for (Class<?> c : new Class<?>[]{BeforeStyleTest.class, CheckoutTest.class, CheckoutIntegrationTest.class}) {
            total = total.plus(MiniTest.run(c));
        }
        System.out.println("TOTAL: " + total.passed() + " passed, " + total.failed() + " failed");
        if (total.failed() > 0) System.exit(1);
    }
}
