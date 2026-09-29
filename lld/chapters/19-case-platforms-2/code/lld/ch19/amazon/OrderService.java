package lld.ch19.amazon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/** Observer: anything that wants to hear about order and shipment changes. */
interface OrderEventListener {
    void onOrderStatus(Order order, OrderStatus status);
    void onShipmentUpdate(Order order, ShipmentLog entry);
}

/** One way of delivering a message (email, SMS, push...). */
interface NotificationChannel {
    void send(Account to, String message);
}

final class EmailChannel implements NotificationChannel {
    @Override public void send(Account to, String message) {
        System.out.println("    [email -> " + to.email() + "] " + message);
    }
}

final class SmsChannel implements NotificationChannel {
    @Override public void send(Account to, String message) {
        System.out.println("    [sms   -> " + to.phone() + "] " + message);
    }
}

/** Requirement 9: tell the member whenever order or shipping status changes. */
final class NotificationService implements OrderEventListener {
    private final Function<String, Account> accountLookup;
    private final List<NotificationChannel> channels;

    NotificationService(Function<String, Account> accountLookup, List<NotificationChannel> channels) {
        this.accountLookup = accountLookup;
        this.channels = List.copyOf(channels);
    }

    @Override public void onOrderStatus(Order order, OrderStatus status) {
        Account to = accountLookup.apply(order.memberId());
        String msg = "Order " + order.orderNumber() + " is now " + status;
        channels.forEach(c -> c.send(to, msg));
    }

    @Override public void onShipmentUpdate(Order order, ShipmentLog entry) {
        Account to = accountLookup.apply(order.memberId());
        channels.get(0).send(to, "Shipment for " + order.orderNumber() + ": " + entry.status() + " @ " + entry.location());
    }
}

/** Application service (facade) that turns a cart into an order and drives the order state machine. */
final class OrderService {
    private final Catalog catalog;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final List<OrderEventListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger orderSeq = new AtomicInteger(1000);
    private final AtomicInteger shipSeq = new AtomicInteger(1);

    OrderService(Catalog catalog) { this.catalog = catalog; }

    void subscribe(OrderEventListener l) { listeners.add(l); }

    /**
     * Checkout. Stock is reserved when the order is placed (not when paid), all-or-nothing,
     * so two buyers can never both take the last copy.
     */
    Order placeOrder(Member member, Address address, Function<BigDecimal, Payment> paymentMethod) {
        if (!member.canBuy()) throw new IllegalStateException("account " + member.account().status());
        List<Item> items = member.cart().snapshot();
        if (items.isEmpty()) throw new IllegalStateException("cart is empty");

        List<Item> reserved = new ArrayList<>();
        for (Item i : items) {
            Product p = catalog.find(i.productId()).orElseThrow();
            if (!p.reserve(i.quantity())) {
                reserved.forEach(r -> catalog.find(r.productId()).orElseThrow().release(r.quantity()));
                throw new IllegalStateException("out of stock: " + i.name());
            }
            reserved.add(i);
        }

        Order order = new Order("ORD-" + orderSeq.incrementAndGet(), member.id(), items, address);
        orders.put(order.orderNumber(), order);

        Payment payment = paymentMethod.apply(order.total());
        order.attachPayment(payment);
        switch (payment.process()) {
            case COMPLETED -> { transition(order, OrderStatus.UNSHIPPED, "paid by " + payment.describe()); member.cart().clear(); }
            case PENDING -> { publish(order, OrderStatus.PENDING); member.cart().clear(); }
            default -> {                                  // declined: give the stock back, keep the cart
                releaseStock(order);
                transition(order, OrderStatus.CANCELED, "payment declined");
            }
        }
        return order;
    }

    /** Called when a bank transfer finally clears. */
    void onPaymentSettled(String orderNumber) {
        Order o = get(orderNumber);
        o.payment().markSettled();
        transition(o, OrderStatus.UNSHIPPED, "transfer cleared");
    }

    /** Requirement 8: cancel only if not shipped yet. */
    boolean cancel(String orderNumber) {
        Order o = get(orderNumber);
        synchronized (o) {                     // check-then-act must be atomic against ship()
            if (!o.canCancel()) return false;
            releaseStock(o);
            transition(o, OrderStatus.CANCELED, "cancelled by member");
        }
        if (o.payment().refund() == PaymentStatus.REFUNDED)
            transition(o, OrderStatus.REFUND_APPLIED, "refunded " + Money.fmt(o.total()));
        return true;
    }

    void ship(String orderNumber, String method) {
        Order o = get(orderNumber);
        Shipment s = new Shipment("SHP-" + shipSeq.getAndIncrement(), method);
        synchronized (o) {
            transition(o, OrderStatus.SHIPPED, "handed to " + method);
            o.attachShipment(s);
        }
        updateShipment(orderNumber, ShipmentStatus.SHIPPED, "sort centre");
    }

    void updateShipment(String orderNumber, ShipmentStatus status, String location) {
        Order o = get(orderNumber);
        o.shipment().addLog(status, location);
        ShipmentLog entry = o.shipment().current();
        listeners.forEach(l -> l.onShipmentUpdate(o, entry));
        if (status == ShipmentStatus.DELIVERED) transition(o, OrderStatus.COMPLETED, "delivered");
    }

    Order get(String orderNumber) {
        Order o = orders.get(orderNumber);
        if (o == null) throw new IllegalArgumentException("no order " + orderNumber);
        return o;
    }

    private void releaseStock(Order o) {
        o.items().forEach(i -> catalog.find(i.productId()).orElseThrow().release(i.quantity()));
    }

    private void transition(Order o, OrderStatus target, String note) {
        o.moveTo(target, note);
        publish(o, target);
    }

    private void publish(Order o, OrderStatus s) { listeners.forEach(l -> l.onOrderStatus(o, s)); }
}
