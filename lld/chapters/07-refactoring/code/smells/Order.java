package lld.ch07.smells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cures for Message Chains (Hide Delegate), Feature Envy (Move Function)
 * and Mutable Data (Encapsulate Collection).
 */
public final class Order {

    public record Address(String street, String city) { }
    public record Customer(String name, Address address) { }
    public record Line(String sku, Money unitPrice, int quantity) {
        /** Moved here from Order: it only used Line's data (Feature Envy cured). */
        public Money total() { return unitPrice.times(quantity); }
    }

    private final Customer customer;
    private final List<Line> lines = new ArrayList<>();

    public Order(Customer customer) { this.customer = customer; }

    /** Hide Delegate: callers ask the order, not order.customer().address().city(). */
    public String shippingCity() { return customer.address().city(); }

    /** Encapsulate Collection: modification goes through the owner... */
    public void add(Line line) { lines.add(line); }

    /** ...and readers get a read-only view. */
    public List<Line> lines() { return Collections.unmodifiableList(lines); }

    public Money total() {
        return lines.stream().map(Line::total).reduce(Money::plus).orElse(Money.of("0", "INR"));
    }
}
