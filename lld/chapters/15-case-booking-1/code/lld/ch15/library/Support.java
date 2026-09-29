package lld.ch15.library;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Strategy: how much a late return costs. Swappable without touching LendingService. */
interface FinePolicy {
    BigDecimal fine(LocalDate dueDate, LocalDate returnDate);
}

/** Flat amount per day late (the classic rule: $0.50 a day). */
final class PerDayFine implements FinePolicy {
    private final BigDecimal perDay;
    PerDayFine(BigDecimal perDay) { this.perDay = perDay; }
    @Override public BigDecimal fine(LocalDate due, LocalDate returned) {
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(due, returned));
        return perDay.multiply(BigDecimal.valueOf(daysLate));
    }
}

/** Observer-style outlet for "your reserved book is back" and "your book is overdue". */
interface Notifier {
    void send(String memberId, String message);
}

final class ConsoleNotifier implements Notifier {
    private final List<String> sent = new CopyOnWriteArrayList<>();
    @Override public void send(String memberId, String message) {
        String line = "  [notify " + memberId + "] " + message;
        sent.add(line);
        System.out.println(line);
    }
    List<String> sent() { return sent; }
}

/** A clock we can move forward in tests and demos, so "10 days later" needs no sleeping. */
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
    /** Money is BigDecimal (exact decimals); shown with exactly two decimal places. */
    static String fmt(BigDecimal amount) { return "$" + amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString(); }
}
