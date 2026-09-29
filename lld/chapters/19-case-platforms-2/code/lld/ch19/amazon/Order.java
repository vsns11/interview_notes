package lld.ch19.amazon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

record Address(String street, String city, String zip, String country) {
    @Override public String toString() { return street + ", " + city + " " + zip + ", " + country; }
}

/** One line of a cart or an order: an immutable snapshot of product, quantity and unit price. */
record Item(String productId, String name, int quantity, BigDecimal unitPrice) {
    Item {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
    }
    BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}

/** A mutable working area. Owns its items (they die when the cart is cleared). */
final class ShoppingCart {
    private final Map<String, Item> items = new LinkedHashMap<>();

    synchronized void addItem(Product p, int qty) {
        items.merge(p.id(), new Item(p.id(), p.name(), qty, p.price()),
                (old, add) -> new Item(old.productId(), old.name(), old.quantity() + add.quantity(), p.price()));
    }

    synchronized void updateQuantity(String productId, int qty) {
        if (qty == 0) { items.remove(productId); return; }
        Item old = items.get(productId);
        if (old == null) throw new IllegalArgumentException("not in cart: " + productId);
        items.put(productId, new Item(old.productId(), old.name(), qty, old.unitPrice()));
    }

    synchronized void removeItem(String productId) { items.remove(productId); }

    /** A copy, so an order never shares its lines with the cart. */
    synchronized List<Item> snapshot() { return List.copyOf(items.values()); }

    synchronized boolean isEmpty() { return items.isEmpty(); }

    synchronized void clear() { items.clear(); }

    synchronized void mergeFrom(ShoppingCart other) {
        for (Item i : other.snapshot()) items.merge(i.productId(), i,
                (a, b) -> new Item(a.productId(), a.name(), a.quantity() + b.quantity(), a.unitPrice()));
    }
}

/** The order state machine. Each state lists the states it may move to. */
enum OrderStatus {
    PENDING, UNSHIPPED, SHIPPED, COMPLETED, CANCELED, REFUND_APPLIED;

    private Set<OrderStatus> next() {
        return switch (this) {
            case PENDING -> EnumSet.of(UNSHIPPED, CANCELED);      // waiting for payment
            case UNSHIPPED -> EnumSet.of(SHIPPED, CANCELED);      // paid, not yet handed to carrier
            case SHIPPED -> EnumSet.of(COMPLETED);                // too late to cancel
            case CANCELED -> EnumSet.of(REFUND_APPLIED);          // money goes back if it was taken
            case COMPLETED, REFUND_APPLIED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    boolean canMoveTo(OrderStatus target) { return next().contains(target); }
}

record OrderLog(OrderStatus status, String note) { }

/** A fixed copy of the purchased lines, plus a status that only moves along legal edges. */
final class Order {
    private final String orderNumber;
    private final String memberId;
    private final List<Item> items;              // copied from the cart, never shared
    private final Address shippingAddress;
    private final List<OrderLog> log = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;
    private Payment payment;
    private Shipment shipment;

    Order(String orderNumber, String memberId, List<Item> items, Address shippingAddress) {
        this.orderNumber = orderNumber;
        this.memberId = memberId;
        this.items = List.copyOf(items);
        this.shippingAddress = shippingAddress;
        log.add(new OrderLog(status, "order placed"));
    }

    String orderNumber() { return orderNumber; }
    String memberId() { return memberId; }
    List<Item> items() { return items; }
    Address shippingAddress() { return shippingAddress; }
    synchronized OrderStatus status() { return status; }
    synchronized Payment payment() { return payment; }
    synchronized Shipment shipment() { return shipment; }
    synchronized List<OrderLog> log() { return Collections.unmodifiableList(new ArrayList<>(log)); }

    BigDecimal total() {
        return items.stream().map(Item::lineTotal).reduce(Money.ZERO, BigDecimal::add);
    }

    synchronized void attachPayment(Payment p) { this.payment = p; }
    synchronized void attachShipment(Shipment s) { this.shipment = s; }

    /** Requirement 8 lives in exactly one place. */
    synchronized boolean canCancel() { return status.canMoveTo(OrderStatus.CANCELED); }

    synchronized void moveTo(OrderStatus target, String note) {
        if (!status.canMoveTo(target))
            throw new IllegalStateException(orderNumber + ": " + status + " -> " + target + " is not allowed");
        status = target;
        log.add(new OrderLog(target, note));
    }
}
