package lld.ch11.architecture.domain;

import java.math.BigDecimal;
import java.util.List;

/** DOMAIN entity. Pure Java: no imports from ports, adapters, frameworks or I/O. */
public final class Order {
    private final String id;
    private final String customerId;
    private final List<OrderLine> lines;
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    private Order(String id, String customerId, List<OrderLine> lines) {
        this.id = id;
        this.customerId = customerId;
        this.lines = lines;
    }

    /** Factory that enforces the invariant "an order has at least one line". */
    public static Order place(String id, String customerId, List<OrderLine> lines) {
        if (lines.isEmpty()) throw new IllegalArgumentException("an order needs at least one line");
        return new Order(id, customerId, List.copyOf(lines));
    }

    public BigDecimal total() {
        return lines.stream().map(OrderLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markPaid() { requirePending(); status = OrderStatus.PAID; }
    public void markPaymentFailed() { requirePending(); status = OrderStatus.PAYMENT_FAILED; }

    private void requirePending() {
        if (status != OrderStatus.PENDING_PAYMENT) throw new IllegalStateException("order " + id + " is already " + status);
    }

    public String id() { return id; }
    public String customerId() { return customerId; }
    public OrderStatus status() { return status; }
}
