package lld.ch19.fooddelivery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Strategy: which free rider gets this order? Returns riders in preference order. */
interface DeliveryAssignmentStrategy {
    List<DeliveryAgent> rank(Order order, List<DeliveryAgent> free);
}

/** Closest rider to the restaurant - fastest pickup. */
final class NearestAgentStrategy implements DeliveryAssignmentStrategy {
    @Override public List<DeliveryAgent> rank(Order o, List<DeliveryAgent> free) {
        Location r = o.restaurant().location();
        return free.stream().sorted(Comparator.comparingDouble(a -> a.location().distanceTo(r))).toList();
    }
}

/** Fewest deliveries today first (spreads earnings), nearest as tie-breaker. */
final class FairShareStrategy implements DeliveryAssignmentStrategy {
    @Override public List<DeliveryAgent> rank(Order o, List<DeliveryAgent> free) {
        Location r = o.restaurant().location();
        return free.stream().sorted(Comparator.comparingInt(DeliveryAgent::deliveriesToday)
                .thenComparingDouble(a -> a.location().distanceTo(r))).toList();
    }
}

/** Observer: customer app, restaurant tablet and rider app all track the order. */
interface OrderListener { void onStatus(Order order, OrderStatus status); }

final class Notifier implements OrderListener {
    @Override public void onStatus(Order o, OrderStatus s) {
        String who = switch (s) {
            case PENDING -> "restaurant " + o.restaurant().name() + ": new order";
            case OUT_FOR_DELIVERY -> "customer " + o.customer().name() + ": " + o.agent().map(DeliveryAgent::name).orElse("?") + " is on the way";
            default -> "customer " + o.customer().name();
        };
        System.out.println("    [" + o.id() + " " + s + "] -> " + who);
    }
}

/** Facade: browse, order, accept, prepare, dispatch, deliver, cancel. */
final class FoodDeliveryService {
    private static final BigDecimal BASE_FEE = new BigDecimal("20.00");
    private static final BigDecimal PER_KM = new BigDecimal("8.00");

    private final Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();
    private final List<DeliveryAgent> agents = new CopyOnWriteArrayList<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Queue<Order> awaitingRider = new ConcurrentLinkedQueue<>();
    private final List<OrderListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger seq = new AtomicInteger(100);
    private volatile DeliveryAssignmentStrategy strategy;

    FoodDeliveryService(DeliveryAssignmentStrategy strategy) { this.strategy = strategy; }

    void setStrategy(DeliveryAssignmentStrategy s) { strategy = s; }
    void subscribe(OrderListener l) { listeners.add(l); }
    void addRestaurant(Restaurant r) { restaurants.put(r.id(), r); }
    void addAgent(DeliveryAgent a) { agents.add(a); }

    /** Open restaurants within the radius, nearest first. */
    List<Restaurant> browse(Customer c, double radiusKm) {
        return restaurants.values().stream().filter(Restaurant::isOpen)
                .filter(r -> r.location().distanceTo(c.address()) <= radiusKm)
                .sorted(Comparator.comparingDouble(r -> r.location().distanceTo(c.address()))).toList();
    }

    Order placeOrder(Customer c, Cart cart, PaymentMethod pay) {
        if (cart.isEmpty()) throw new IllegalStateException("empty cart");
        Restaurant r = cart.restaurant();
        if (!r.isOpen()) throw new IllegalStateException(r.name() + " is closed");
        List<OrderItem> items = new ArrayList<>();
        cart.quantities().forEach((itemId, qty) -> {
            MenuItem m = r.item(itemId).orElseThrow();
            if (!m.available()) throw new IllegalStateException(m.name() + " is sold out");
            items.add(new OrderItem(itemId, m.name(), m.price(), qty));
        });
        BigDecimal km = BigDecimal.valueOf(r.location().distanceTo(c.address())).setScale(1, RoundingMode.HALF_UP);
        BigDecimal fee = BASE_FEE.add(PER_KM.multiply(km)).setScale(2, RoundingMode.HALF_EVEN);
        Order o = new Order("OD" + seq.incrementAndGet(), c, r, items, fee, pay);
        orders.put(o.id(), o);
        cart.clear();
        publish(o, OrderStatus.PENDING);
        return o;
    }

    /** The restaurant accepts only if its kitchen has room; otherwise the order is auto-cancelled. */
    boolean restaurantAccepts(String orderId) {
        Order o = orders.get(orderId);
        synchronized (o) {                              // same lock as cancel(): capacity can never leak
            if (o.status() != OrderStatus.PENDING) return false;
            if (!o.restaurant().tryReserveKitchen(o.itemCount())) {
                System.out.println("    (" + o.restaurant().name() + " kitchen full: needs " + o.itemCount()
                        + " item slots, free " + o.restaurant().freeCapacity() + ")");
                transition(o, OrderStatus.CANCELLED);
                return false;
            }
            transition(o, OrderStatus.CONFIRMED);
            return true;
        }
    }

    void startPreparing(String orderId) { transition(orders.get(orderId), OrderStatus.PREPARING); }

    /** Food is ready: kitchen capacity comes back, and we look for a rider. */
    void foodReady(String orderId) {
        Order o = orders.get(orderId);
        transition(o, OrderStatus.READY_FOR_PICKUP);
        o.restaurant().releaseKitchen(o.itemCount());
        awaitingRider.add(o);
        dispatchWaiting();
    }

    void delivered(String orderId) {
        Order o = orders.get(orderId);
        transition(o, OrderStatus.DELIVERED);
        DeliveryAgent a = o.agent().orElseThrow();
        a.moveTo(o.customer().address());
        a.completeDelivery();
        dispatchWaiting();                              // a freed rider takes the oldest waiting order
    }

    boolean cancel(String orderId) {
        Order o = orders.get(orderId);
        synchronized (o) {
            if (!o.status().canMoveTo(OrderStatus.CANCELLED)) return false;
            boolean kitchenHeld = o.status() == OrderStatus.CONFIRMED;
            transition(o, OrderStatus.CANCELLED);
            if (kitchenHeld) o.restaurant().releaseKitchen(o.itemCount());
        }
        if (o.payment().paidUpfront()) System.out.println("    refund " + o.total() + " to " + o.payment().describe());
        return true;
    }

    Order order(String id) { return orders.get(id); }

    /** Hand waiting orders (oldest first) to free riders. CAS on the rider prevents double booking. */
    private synchronized void dispatchWaiting() {
        Order o;
        while ((o = awaitingRider.peek()) != null) {
            List<DeliveryAgent> free = agents.stream().filter(DeliveryAgent::isAvailable).toList();
            Optional<DeliveryAgent> got = Optional.empty();
            for (DeliveryAgent a : strategy.rank(o, free)) if (a.tryClaim()) { got = Optional.of(a); break; }
            if (got.isEmpty()) {
                System.out.println("    (" + o.id() + " waiting for a free rider)");
                return;
            }
            awaitingRider.poll();
            o.assign(got.get());
            transition(o, OrderStatus.OUT_FOR_DELIVERY);
        }
    }

    private void transition(Order o, OrderStatus s) {
        o.moveTo(s);
        listeners.forEach(l -> l.onStatus(o, s));
    }

    private void publish(Order o, OrderStatus s) { listeners.forEach(l -> l.onStatus(o, s)); }
}
