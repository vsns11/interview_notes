package lld.ch15.moviebooking;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

enum BookingStatus { PENDING, CONFIRMED, CHECKED_IN, CANCELED, ABANDONED }

enum AccountStatus { ACTIVE, BLOCKED, BANNED, COMPROMISED, ARCHIVED, UNKNOWN }

/** Only registered customers can book. A guest has no type that BookingService accepts. */
final class Customer {
    private final String id;
    private final String name;
    private volatile AccountStatus status = AccountStatus.ACTIVE;
    Customer(String id, String name) { this.id = id; this.name = name; }
    String id()                 { return id; }
    String name()               { return name; }
    AccountStatus status()      { return status; }
    void setStatus(AccountStatus s) { status = s; }
}

record Guest(String sessionId) {
    Customer register(String id, String name) { return new Customer(id, name); }
}

record FrontDeskOfficer(String id, String name) {}

record Admin(String id, String name) {
    void block(Customer c)   { c.setStatus(AccountStatus.BLOCKED); }
    void unblock(Customer c) { c.setStatus(AccountStatus.ACTIVE); }
}

final class SeatUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    SeatUnavailableException(String msg) { super(msg); }
}

/** A booking points at the show and seats; it owns none of them. Transitions run under its own monitor. */
final class Booking {
    private final String id;
    private final String customerId;
    private final Show show;
    private final List<String> seatIds;
    private final BigDecimal baseAmount;
    private BookingStatus status = BookingStatus.PENDING;
    private Payment payment;
    private Coupon coupon;

    Booking(String id, String customerId, Show show, List<String> seatIds, BigDecimal baseAmount) {
        this.id = id; this.customerId = customerId; this.show = show;
        this.seatIds = List.copyOf(seatIds); this.baseAmount = baseAmount;
    }
    String id()               { return id; }
    String customerId()       { return customerId; }
    Show show()               { return show; }
    List<String> seatIds()    { return seatIds; }
    BigDecimal baseAmount()   { return baseAmount; }
    synchronized BookingStatus status() { return status; }
    synchronized Payment payment()      { return payment; }
    synchronized Coupon coupon()        { return coupon; }
    void setStatus(BookingStatus s)     { status = s; }          // called with the monitor held
    void setPayment(Payment p)          { payment = p; }
    void setCoupon(Coupon c)            { coupon = c; }
    @Override public synchronized String toString() {
        return id + " " + seatIds + " " + status + (payment == null ? "" : " " + payment.status() + " " + Money.fmt(payment.amount()));
    }
}

/**
 * The booking path: HOLD seats (TTL) -> PAY -> COMMIT seats. If the hold was lost while the bank
 * was charging, the money is refunded (compensation) instead of double-selling the seat.
 */
final class BookingService {
    private final SeatLockProvider locks;
    private final PaymentGateway gateway;
    private final CancellationPolicy cancellationPolicy;
    private final Notifier notifier;
    private final Clock clock;
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger();

    BookingService(SeatLockProvider locks, PaymentGateway gateway, CancellationPolicy policy, Notifier notifier, Clock clock) {
        this.locks = locks; this.gateway = gateway; this.cancellationPolicy = policy;
        this.notifier = notifier; this.clock = clock;
    }

    /** Step 1: hold the seats. All-or-nothing. Throws if any seat is booked or held by someone else. */
    Booking hold(Customer c, Show show, List<String> seatIds) {
        if (c.status() != AccountStatus.ACTIVE) throw new IllegalStateException(c.id() + " is " + c.status());
        if (seatIds.isEmpty()) throw new IllegalArgumentException("no seats");
        List<String> sorted = seatIds.stream().distinct().sorted().toList();
        if (!locks.lock(show, sorted, c.id()))
            throw new SeatUnavailableException(sorted + " not available for " + show.id());
        BigDecimal base = sorted.stream().map(id -> show.seat(id).price()).reduce(BigDecimal.ZERO, BigDecimal::add);
        Booking b = new Booking("BK-" + seq.incrementAndGet(), c.id(), show, sorted, base);
        bookings.put(b.id(), b);
        return b;
    }

    /** The front desk books for a walk-in customer: same flow, different actor. */
    Booking holdFor(FrontDeskOfficer officer, Customer c, Show show, List<String> seatIds) {
        return hold(c, show, seatIds);
    }

    /** Step 2 + 3: pay, then commit. Idempotent: paying a confirmed booking again returns it unchanged. */
    Booking pay(String bookingId, PaymentMethod method, Coupon coupon) {
        Booking b = get(bookingId);
        synchronized (b) {
            if (b.status() != BookingStatus.PENDING) return b;
            if (!locks.holds(b.show(), b.seatIds(), b.customerId())) {       // fast fail, no charge
                b.setStatus(BookingStatus.ABANDONED);
                notifier.send(b.customerId(), b.id() + " hold expired before payment, nothing charged");
                return b;
            }
            b.setCoupon(coupon);
            BigDecimal amount = coupon == null ? b.baseAmount() : coupon.apply(b.baseAmount());
            Payment p = gateway.charge(b.id(), amount, method);
            b.setPayment(p);
            if (p.status() != PaymentStatus.COMPLETED) {
                locks.unlock(b.show(), b.seatIds(), b.customerId());           // free seats at once
                b.setStatus(BookingStatus.ABANDONED);
                notifier.send(b.customerId(), b.id() + " payment " + p.status() + ", seats released");
                return b;
            }
            if (!locks.commit(b.show(), b.seatIds(), b.customerId())) {       // hold lost during charge
                b.setPayment(gateway.refund(p));                              // COMPENSATION
                b.setStatus(BookingStatus.ABANDONED);
                notifier.send(b.customerId(), b.id() + " hold expired during payment, " + Money.fmt(amount) + " refunded");
                return b;
            }
            b.setStatus(BookingStatus.CONFIRMED);
            notifier.send(b.customerId(), b.id() + " CONFIRMED " + b.seatIds() + " for '" + b.show().movie().title()
                    + "' at " + b.show().start() + ", paid " + Money.fmt(amount));
            return b;
        }
    }

    /** Cancel: release seats and refund according to the policy. */
    Booking cancel(String bookingId) {
        Booking b = get(bookingId);
        synchronized (b) {
            switch (b.status()) {
                case PENDING -> {
                    locks.unlock(b.show(), b.seatIds(), b.customerId());
                    b.setStatus(BookingStatus.CANCELED);
                }
                case CONFIRMED -> {
                    locks.releaseBooked(b.show(), b.seatIds());
                    BigDecimal refund = cancellationPolicy.refund(b.payment().amount(),
                            LocalDateTime.now(clock), b.show().start());
                    if (refund.signum() > 0) b.setPayment(gateway.refund(b.payment()));
                    b.setStatus(BookingStatus.CANCELED);
                    notifier.send(b.customerId(), b.id() + " canceled, refund " + Money.fmt(refund));
                }
                default -> throw new IllegalStateException(b.id() + " cannot be canceled from " + b.status());
            }
            return b;
        }
    }

    Booking get(String id) {
        Booking b = bookings.get(id);
        if (b == null) throw new IllegalArgumentException("no booking " + id);
        return b;
    }

    /** "." free, "L" held by someone paying, "X" booked. */
    String seatMap(Show show) {
        Set<String> held = locks.lockedSeats(show);
        StringBuilder sb = new StringBuilder();
        char row = 0;
        for (ShowSeat s : show.seats().values()) {
            String id = s.seat().id();
            if (id.charAt(0) != row) { if (row != 0) sb.append('\n'); row = id.charAt(0); sb.append("  ").append(row).append(' '); }
            sb.append(s.isBooked() ? 'X' : held.contains(id) ? 'L' : '.').append(' ');
        }
        return sb.toString();
    }
}
