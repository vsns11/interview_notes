package lld.ch07.smells;

import java.time.LocalDate;

/** Small, runnable cures for common smells. */
public class Main {
    public static void main(String[] args) {
        // Primitive Obsession -> Money: 0.1 + 0.2 is exact, currencies cannot be mixed by accident
        System.out.println("double 0.1+0.2       = " + (0.1 + 0.2));
        System.out.println("Money  0.10+0.20     = " + Money.of("0.10", "USD").plus(Money.of("0.20", "USD")));
        try {
            Money.of("1", "USD").plus(Money.of("1", "EUR"));
        } catch (IllegalArgumentException e) {
            System.out.println("mixing currencies    -> " + e.getMessage());
        }

        // Data Clumps -> DateRange
        DateRange booking = new DateRange(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5));
        DateRange other = new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8));
        System.out.println("booking contains 3rd = " + booking.contains(LocalDate.of(2026, 10, 3))
                + ", overlaps other = " + booking.overlaps(other));

        // Message Chains, Feature Envy, Mutable Data
        Order order = new Order(new Order.Customer("Asha", new Order.Address("MG Road", "Bengaluru")));
        order.add(new Order.Line("PEN", Money.of("25.50", "INR"), 4));
        order.add(new Order.Line("BOOK", Money.of("199", "INR"), 1));
        System.out.println("ship to " + order.shippingCity() + ", total " + order.total());
        try {
            order.lines().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("outsider tried lines().clear() -> UnsupportedOperationException (collection is encapsulated)");
        }
    }
}
