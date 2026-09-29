package lld.ch20.notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Strategy: one reason not to send. Returns the reason, or empty to let it through. */
interface SendRule {
    Optional<String> blocks(NotificationRequest request, Preferences prefs, LocalDateTime now);

    /** Told after a successful send (so suppressed messages do not count). */
    default void onSent(NotificationRequest request, LocalDateTime now) {}
}

/** Nothing normal during quiet hours. The rule itself knows that URGENT ignores it. */
final class QuietHoursRule implements SendRule {
    @Override
    public Optional<String> blocks(NotificationRequest r, Preferences prefs, LocalDateTime now) {
        if (r.priority() == Priority.URGENT) return Optional.empty();
        return prefs.isQuiet(now.getHour()) ? Optional.of("quiet hours") : Optional.empty();
    }
}

/** At most 'limit' notifications of one kind per user per sliding window. */
final class RateLimitRule implements SendRule {
    private final int limit;
    private final Duration window;
    private final Map<String, Deque<LocalDateTime>> sent = new ConcurrentHashMap<>();

    RateLimitRule(int limit, Duration window) {
        this.limit = limit;
        this.window = window;
    }

    private static String key(NotificationRequest r) { return r.userId() + "|" + r.kind(); }

    @Override
    public Optional<String> blocks(NotificationRequest r, Preferences prefs, LocalDateTime now) {
        Deque<LocalDateTime> times = sent.computeIfAbsent(key(r), k -> new ArrayDeque<>());
        synchronized (times) {
            prune(times, now); // old timestamps are dropped, so one key's deque never exceeds 'limit'
            return times.size() >= limit
                    ? Optional.of("rate limit " + limit + " per " + window.toMinutes() + " min")
                    : Optional.empty();
        }
    }

    @Override
    public void onSent(NotificationRequest r, LocalDateTime now) {
        Deque<LocalDateTime> times = sent.computeIfAbsent(key(r), k -> new ArrayDeque<>());
        synchronized (times) {
            times.addLast(now);
        }
    }

    private void prune(Deque<LocalDateTime> times, LocalDateTime now) {
        LocalDateTime cutoff = now.minus(window);
        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) times.pollFirst();
    }
}

/** Observer: told the outcome of every notification. */
interface DeliveryListener {
    void onOutcome(DeliveryRecord record);
}

final class LoggingListener implements DeliveryListener {
    @Override
    public void onOutcome(DeliveryRecord r) {
        System.out.printf("  [log] %-10s %-13s user=%s channel=%s attempts=%d%s%n", r.status(), r.kind(),
                r.userId(), r.channel() == null ? "-" : r.channel(), r.attempts(),
                r.reason() == null ? "" : " reason=" + r.reason());
    }
}

final class MetricsListener implements DeliveryListener {
    private final Map<DeliveryStatus, Integer> counts = new ConcurrentHashMap<>();

    @Override
    public void onOutcome(DeliveryRecord r) { counts.merge(r.status(), 1, Integer::sum); }

    Map<DeliveryStatus, Integer> counts() { return new java.util.TreeMap<>(counts); }
}
