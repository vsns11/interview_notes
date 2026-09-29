package lld.ch15.moviebooking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

enum PaymentStatus { UNPAID, PENDING, COMPLETED, DECLINED, CANCELLED, REFUNDED }

/** How the customer pays. Sealed: the system supports exactly these two (credit card or cash). */
sealed interface PaymentMethod permits CardPayment, CashPayment {}
record CardPayment(String cardNumberLast4) implements PaymentMethod {}
record CashPayment() implements PaymentMethod {}

record Payment(String txnId, BigDecimal amount, PaymentMethod method, PaymentStatus status) {
    Payment withStatus(PaymentStatus s) { return new Payment(txnId, amount, method, s); }
}

/** Adapter boundary to the real payment provider. */
interface PaymentGateway {
    Payment charge(String bookingId, BigDecimal amount, PaymentMethod method);
    Payment refund(Payment p);
}

/** Test double: declines listed cards, and can run a hook "while the bank is thinking". */
final class FakeGateway implements PaymentGateway {
    private final Set<String> declinedCards;
    private final AtomicInteger seq = new AtomicInteger();
    private volatile Runnable duringCharge = () -> {};

    FakeGateway(Set<String> declinedCards) { this.declinedCards = declinedCards; }
    void setDuringCharge(Runnable r) { duringCharge = r; }

    @Override public Payment charge(String bookingId, BigDecimal amount, PaymentMethod m) {
        duringCharge.run();
        String txn = "TXN-" + seq.incrementAndGet();
        boolean declined = m instanceof CardPayment c && declinedCards.contains(c.cardNumberLast4());
        return new Payment(txn, amount, m, declined ? PaymentStatus.DECLINED : PaymentStatus.COMPLETED);
    }
    @Override public Payment refund(Payment p) { return p.withStatus(PaymentStatus.REFUNDED); }
}

/** A percentage off. Applied to the booking amount BEFORE payment, so every payment type sees the right total. */
record Coupon(String code, int percentOff) {
    Coupon {
        if (percentOff < 0 || percentOff > 100) throw new IllegalArgumentException("bad percent " + percentOff);
    }
    /** Amount after the discount, rounded to whole paise/cents (half-up). */
    BigDecimal apply(BigDecimal amount) {
        BigDecimal off = amount.multiply(BigDecimal.valueOf(percentOff)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return amount.subtract(off).setScale(2, RoundingMode.HALF_UP);
    }
}

/** Strategy: how much of the money comes back on cancellation. */
interface CancellationPolicy {
    BigDecimal refund(BigDecimal paid, LocalDateTime now, LocalDateTime showStart);
}

/** Full refund up to `cutoff` before the show, nothing after. */
record CutoffRefundPolicy(Duration cutoff) implements CancellationPolicy {
    @Override public BigDecimal refund(BigDecimal paid, LocalDateTime now, LocalDateTime showStart) {
        return now.plus(cutoff).isAfter(showStart) ? BigDecimal.ZERO : paid;
    }
}

interface Notifier { void send(String userId, String message); }

final class ConsoleNotifier implements Notifier {
    @Override public void send(String userId, String message) {
        System.out.println("  [notify " + userId + "] " + message);
    }
}

final class SimClock extends Clock {
    private volatile Instant now;
    SimClock(Instant start) { this.now = start; }
    void advance(Duration d) { now = now.plus(d); }
    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return now; }
}

final class Money {
    private Money() {}
    static String fmt(BigDecimal amount) { return "Rs." + amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString(); }
}
