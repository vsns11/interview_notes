package lld.ch16.restaurant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

record MealItem(MenuItem item, int quantity) {
    MealItem {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
    }
    BigDecimal subtotal() { return item.price().multiply(BigDecimal.valueOf(quantity)); }
}

/** One meal per seat: lets the kitchen plate per guest and lets the bill be split. */
final class Meal {
    private final int seat;
    private final List<MealItem> items = new ArrayList<>();
    Meal(int seat) { this.seat = seat; }
    int seat() { return seat; }
    synchronized void add(MealItem i) { items.add(i); }
    synchronized List<MealItem> items() { return List.copyOf(items); }
    synchronized BigDecimal subtotal() { return items.stream().map(MealItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
}

/** Observer: whoever cares about an order's progress (the waiter, a display screen). */
interface OrderListener { void onStatusChange(Order order, OrderStatus newStatus); }

final class Order {
    private final String id;
    private final Table table;
    private final Map<Integer, Meal> meals = new TreeMap<>();
    private final List<OrderListener> listeners = new CopyOnWriteArrayList<>();
    private OrderStatus status = OrderStatus.RECEIVED;

    Order(String id, Table table) { this.id = id; this.table = table; }
    String id() { return id; }
    Table table() { return table; }
    synchronized OrderStatus status() { return status; }
    void subscribe(OrderListener l) { listeners.add(l); }

    /** Items can only be added before the kitchen starts cooking. */
    synchronized void add(int seat, MenuItem item, int qty) {
        if (status != OrderStatus.RECEIVED) throw new IllegalStateException("order " + id + " is already " + status);
        if (seat < 1 || seat > table.capacity()) throw new IllegalArgumentException("table " + table.id() + " has no seat " + seat);
        meals.computeIfAbsent(seat, Meal::new).add(new MealItem(item, qty));
    }

    synchronized List<Meal> meals() { return List.copyOf(meals.values()); }

    BigDecimal subtotal() { return meals().stream().map(Meal::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }

    void moveTo(OrderStatus next) {
        synchronized (this) {
            boolean ok = switch (status) {
                case RECEIVED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
                case PREPARING -> next == OrderStatus.READY;
                case READY -> next == OrderStatus.SERVED;
                case SERVED, CANCELLED -> false;
            };
            if (!ok) throw new IllegalStateException("order " + id + ": " + status + " -> " + next + " not allowed");
            status = next;
        }
        listeners.forEach(l -> l.onStatusChange(this, next));   // notify outside the lock
    }
}
