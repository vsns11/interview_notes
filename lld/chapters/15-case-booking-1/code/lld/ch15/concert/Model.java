package lld.ch15.concert;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

/** Seated tiers have numbered seats; general admission (GA) is just a count of standing places. */
enum Tier {
    VIP(true, "5000.00"), PREMIUM(true, "2500.00"), GENERAL_ADMISSION(false, "800.00");
    final boolean seated;
    final BigDecimal price;                       // money: BigDecimal, never double
    Tier(boolean seated, String price) { this.seated = seated; this.price = new BigDecimal(price); }
    BigDecimal priceFor(int qty) { return price.multiply(BigDecimal.valueOf(qty)); }
}

record Concert(String id, String artist, String venue, LocalDateTime startsAt) {}

/** A seat's whole life in one immutable value, swapped atomically with compare-and-set (CAS). */
sealed interface SeatState permits Free, Held, Sold {}
record Free() implements SeatState {}
record Held(String holdId, String userId, Instant expiresAt) implements SeatState {
    boolean expired(Instant now) { return !now.isBefore(expiresAt); }
}
record Sold(String holdId, String userId) implements SeatState {}

/** A temporary claim on inventory: either specific seats or a GA quantity. */
record Hold(String id, String userId, Tier tier, List<String> seatIds, int quantity, BigDecimal amount, Instant expiresAt) {
    boolean expired(Instant now) { return !now.isBefore(expiresAt); }
}

record Ticket(String id, String concertId, String userId, Tier tier, String seatOrGa) {}

final class SoldOutException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    SoldOutException(String msg) { super(msg); }
}

interface PaymentGateway {
    boolean charge(String userId, BigDecimal amount);
    void refund(String userId, BigDecimal amount);
}

interface Notifier { void send(String userId, String message); }

final class SimClock extends Clock {
    private volatile Instant now;
    SimClock(Instant start) { this.now = start; }
    void advance(Duration d) { now = now.plus(d); }
    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return now; }
}
