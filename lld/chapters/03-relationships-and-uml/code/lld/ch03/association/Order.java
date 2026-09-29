package lld.ch03.association;

import java.util.Objects;

/**
 * Uni-directional association, multiplicity "many orders to exactly 1 customer".
 * Order holds a reference to Customer; Customer has no field pointing back.
 */
public final class Order {
    private final String id;
    private final Customer customer;   // "1" end: never null

    public Order(String id, Customer customer) {
        this.id = id;
        this.customer = Objects.requireNonNull(customer, "an order needs exactly one customer");
    }

    public String id() { return id; }
    public Customer customer() { return customer; }
}
