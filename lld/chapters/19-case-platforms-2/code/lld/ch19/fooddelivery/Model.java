package lld.ch19.fooddelivery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** A point on a simple km grid (real systems use lat/long and a road-distance service). */
record Location(double x, double y) {
    double distanceTo(Location o) { return Math.hypot(x - o.x, y - o.y); }
}

record Customer(String id, String name, String phone, Location address) { }

final class MenuItem {
    private final String id;
    private final String name;
    private volatile BigDecimal price;
    private volatile boolean available = true;

    MenuItem(String id, String name, BigDecimal price) { this.id = id; this.name = name; this.price = price; }
    String id() { return id; }
    String name() { return name; }
    BigDecimal price() { return price; }
    boolean available() { return available; }
    void setPrice(BigDecimal p) { price = p; }
    void setAvailable(boolean a) { available = a; }
}

/**
 * A restaurant manages its own menu and has a kitchen capacity: at most N items being
 * prepared at once. Capacity is taken when an order is accepted and returned when the
 * food is ready (dispatched).
 */
final class Restaurant {
    private final String id;
    private final String name;
    private final Location location;
    private final int kitchenCapacity;
    private final Map<String, MenuItem> menu = new ConcurrentHashMap<>();
    private volatile boolean open = true;
    private int itemsInKitchen;                         // guarded by this

    Restaurant(String id, String name, Location location, int kitchenCapacity) {
        this.id = id; this.name = name; this.location = location; this.kitchenCapacity = kitchenCapacity;
    }

    String id() { return id; }
    String name() { return name; }
    Location location() { return location; }
    boolean isOpen() { return open; }
    void setOpen(boolean o) { open = o; }
    void addItem(MenuItem m) { menu.put(m.id(), m); }
    Optional<MenuItem> item(String itemId) { return Optional.ofNullable(menu.get(itemId)); }
    Collection<MenuItem> menu() { return menu.values(); }

    synchronized boolean tryReserveKitchen(int items) {
        if (itemsInKitchen + items > kitchenCapacity) return false;
        itemsInKitchen += items;
        return true;
    }

    synchronized void releaseKitchen(int items) { itemsInKitchen -= items; }

    synchronized int freeCapacity() { return kitchenCapacity - itemsInKitchen; }
}

/** A customer's cart. One restaurant per cart, as in Swiggy/Zomato. */
final class Cart {
    private Restaurant restaurant;
    private final Map<String, Integer> quantities = new LinkedHashMap<>();

    synchronized void add(Restaurant r, String itemId, int qty) {
        if (restaurant != null && !restaurant.id().equals(r.id()))
            throw new IllegalStateException("cart has items from " + restaurant.name() + " - clear it first");
        r.item(itemId).orElseThrow(() -> new IllegalArgumentException("no item " + itemId));
        restaurant = r;
        quantities.merge(itemId, qty, Integer::sum);
    }

    synchronized void clear() { restaurant = null; quantities.clear(); }
    synchronized Restaurant restaurant() { return restaurant; }
    synchronized Map<String, Integer> quantities() { return Map.copyOf(quantities); }
    synchronized boolean isEmpty() { return quantities.isEmpty(); }
}

/** Price is copied at order time: a later menu price change never alters a placed order. */
record OrderItem(String itemId, String name, BigDecimal unitPrice, int quantity) {
    BigDecimal total() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}

/** The order-tracking state machine. */
enum OrderStatus {
    PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED;

    private Set<OrderStatus> next() {
        return switch (this) {
            case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(PREPARING, CANCELLED);
            case PREPARING -> EnumSet.of(READY_FOR_PICKUP);
            case READY_FOR_PICKUP -> EnumSet.of(OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> EnumSet.of(DELIVERED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    boolean canMoveTo(OrderStatus t) { return next().contains(t); }
}

/** Strategy: how the customer pays. */
sealed interface PaymentMethod permits UpiPayment, CardPayment, CashOnDelivery {
    String describe();
    default boolean paidUpfront() { return true; }
}

record UpiPayment(String vpa) implements PaymentMethod {
    public String describe() { return "UPI " + vpa; }
}

record CardPayment(String last4) implements PaymentMethod {
    public String describe() { return "card ****" + last4; }
}

record CashOnDelivery() implements PaymentMethod {
    public String describe() { return "cash on delivery"; }
    public boolean paidUpfront() { return false; }
}

final class Order {
    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final List<OrderItem> items;
    private final BigDecimal deliveryFee;
    private final PaymentMethod payment;
    private final List<OrderStatus> history = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;
    private DeliveryAgent agent;

    Order(String id, Customer c, Restaurant r, List<OrderItem> items, BigDecimal fee, PaymentMethod payment) {
        this.id = id; this.customer = c; this.restaurant = r; this.items = List.copyOf(items);
        this.deliveryFee = fee; this.payment = payment;
        history.add(status);
    }

    String id() { return id; }
    Customer customer() { return customer; }
    Restaurant restaurant() { return restaurant; }
    List<OrderItem> items() { return items; }
    PaymentMethod payment() { return payment; }
    int itemCount() { return items.stream().mapToInt(OrderItem::quantity).sum(); }
    BigDecimal subtotal() { return items.stream().map(OrderItem::total).reduce(BigDecimal.ZERO, BigDecimal::add); }
    BigDecimal total() { return subtotal().add(deliveryFee).setScale(2, RoundingMode.HALF_EVEN); }
    BigDecimal deliveryFee() { return deliveryFee; }
    synchronized OrderStatus status() { return status; }
    synchronized List<OrderStatus> history() { return List.copyOf(history); }
    synchronized Optional<DeliveryAgent> agent() { return Optional.ofNullable(agent); }
    synchronized void assign(DeliveryAgent a) { agent = a; }

    synchronized void moveTo(OrderStatus t) {
        if (!status.canMoveTo(t)) throw new IllegalStateException(id + ": " + status + " -> " + t + " not allowed");
        status = t;
        history.add(t);
    }
}

/** A rider. available is an AtomicBoolean so two orders can never claim the same rider. */
final class DeliveryAgent {
    private final String id;
    private final String name;
    private volatile Location location;
    private final AtomicBoolean available = new AtomicBoolean(true);
    private final AtomicInteger deliveriesToday = new AtomicInteger();

    DeliveryAgent(String id, String name, Location location) { this.id = id; this.name = name; this.location = location; }
    String id() { return id; }
    String name() { return name; }
    Location location() { return location; }
    void moveTo(Location l) { location = l; }
    boolean isAvailable() { return available.get(); }
    int deliveriesToday() { return deliveriesToday.get(); }

    /** Compare-and-set: exactly one caller wins. */
    boolean tryClaim() { return available.compareAndSet(true, false); }

    void completeDelivery() { deliveriesToday.incrementAndGet(); available.set(true); }

    @Override public String toString() { return name; }
}
