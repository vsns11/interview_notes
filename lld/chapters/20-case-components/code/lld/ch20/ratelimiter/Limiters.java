package lld.ch20.ratelimiter;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Where time comes from (nanoseconds). Injected so tests do not sleep. */
interface Ticker { long nanos(); }

/** The answer to one request: allowed or not, what is left, and when to retry (HTTP 429 Retry-After). */
record Decision(boolean allowed, long remaining, Duration retryAfter) {
    static Decision allow(long remaining) { return new Decision(true, remaining, Duration.ZERO); }
    static Decision deny(Duration retryAfter) { return new Decision(false, 0, retryAfter); }
}

/**
 * Strategy: one algorithm, one instance per key (user, API key, IP...). Must be thread-safe.
 * Two-phase so that limits can be combined without side effects:
 * probe() answers "would one more request be allowed now?" WITHOUT using up anything,
 * commit() records the request. Both must be called while holding this object's lock.
 */
interface Limiter {
    Decision probe();

    void commit();

    /** Check and take in ONE atomic step (probe + commit under this limiter's lock). */
    default Decision tryAcquire() {
        synchronized (this) {
            Decision d = probe();
            if (d.allowed()) commit();
            return d;
        }
    }
}

/** Token bucket: bursts up to 'capacity', refills at 'perSecond'. Lazy refill, no background thread. */
final class TokenBucket implements Limiter {
    private final long capacity;
    private final double refillPerNano;
    private final Ticker ticker;
    private double tokens;
    private long last;

    TokenBucket(long capacity, double perSecond, Ticker ticker) {
        this.capacity = capacity;
        this.refillPerNano = perSecond / 1e9;
        this.ticker = ticker;
        this.tokens = capacity;
        this.last = ticker.nanos();
    }

    @Override
    public synchronized Decision probe() {
        long now = ticker.nanos();
        tokens = Math.min(capacity, tokens + (now - last) * refillPerNano); // refilling is safe to do early
        last = now;
        if (tokens >= 1) return Decision.allow((long) (tokens - 1));
        long waitNanos = refillPerNano == 0 ? Long.MAX_VALUE : (long) Math.ceil((1 - tokens) / refillPerNano);
        return Decision.deny(Duration.ofNanos(waitNanos));
    }

    @Override
    public synchronized void commit() { tokens -= 1; }
}

/** Fixed window counter: simplest; allows up to 2x the limit across a window boundary. */
final class FixedWindow implements Limiter {
    private final long limit;
    private final long windowNanos;
    private final Ticker ticker;
    private long windowStart;
    private long count;

    FixedWindow(long limit, Duration window, Ticker ticker) {
        this.limit = limit;
        this.windowNanos = window.toNanos();
        this.ticker = ticker;
    }

    @Override
    public synchronized Decision probe() {
        long now = ticker.nanos();
        long currentWindow = now - Math.floorMod(now, windowNanos);
        if (currentWindow != windowStart) {
            windowStart = currentWindow;
            count = 0;
        }
        if (count < limit) return Decision.allow(limit - count - 1);
        return Decision.deny(Duration.ofNanos(windowStart + windowNanos - now));
    }

    @Override
    public synchronized void commit() { count++; }
}

/** Sliding window log: exact, but stores one timestamp per allowed request. */
final class SlidingWindowLog implements Limiter {
    private final long limit;
    private final long windowNanos;
    private final Ticker ticker;
    private final Deque<Long> log = new ArrayDeque<>();
    private long probedAt;                        // commit() records the time the probe saw

    SlidingWindowLog(long limit, Duration window, Ticker ticker) {
        this.limit = limit;
        this.windowNanos = window.toNanos();
        this.ticker = ticker;
    }

    @Override
    public synchronized Decision probe() {
        long now = ticker.nanos();
        probedAt = now;
        while (!log.isEmpty() && log.peekFirst() <= now - windowNanos) log.pollFirst();
        if (log.size() < limit) return Decision.allow(limit - log.size() - 1);
        return Decision.deny(Duration.ofNanos(log.peekFirst() + windowNanos - now));
    }

    @Override
    public synchronized void commit() { log.addLast(probedAt); }
}

/** Sliding window counter: two counters, weights the previous window by how much of it still overlaps. */
final class SlidingWindowCounter implements Limiter {
    private final long limit;
    private final long windowNanos;
    private final Ticker ticker;
    private long currentStart;
    private long current, previous;

    SlidingWindowCounter(long limit, Duration window, Ticker ticker) {
        this.limit = limit;
        this.windowNanos = window.toNanos();
        this.ticker = ticker;
    }

    @Override
    public synchronized Decision probe() {
        long now = ticker.nanos();
        long start = now - Math.floorMod(now, windowNanos);
        if (start != currentStart) {
            previous = (start - currentStart == windowNanos) ? current : 0; // older than one window: forget it
            current = 0;
            currentStart = start;
        }
        double overlap = 1.0 - (double) (now - currentStart) / windowNanos;
        double estimate = previous * overlap + current;
        if (estimate + 1 <= limit) return Decision.allow((long) (limit - estimate - 1));
        return Decision.deny(Duration.ofNanos(currentStart + windowNanos - now));
    }

    @Override
    public synchronized void commit() { current++; }
}

/**
 * Composite: allowed only if EVERY child allows (e.g. 10/second AND 1000/day).
 * Check-all-then-commit under one lock: a request that ANY child denies uses up NOTHING,
 * so a denied request can never eat a slot in an earlier child.
 * The children are created for this composite only (by LimiterFactory) and never shared,
 * so holding the composite's lock is enough to make probe-all + commit-all atomic.
 */
final class CompositeLimiter implements Limiter {
    private final List<Limiter> children;

    CompositeLimiter(List<Limiter> children) { this.children = List.copyOf(children); }

    @Override
    public synchronized Decision probe() {
        long remaining = Long.MAX_VALUE;
        for (Limiter l : children) {
            Decision d;
            synchronized (l) { d = l.probe(); }
            if (!d.allowed()) return d;           // first denial wins; nothing has been consumed
            remaining = Math.min(remaining, d.remaining());
        }
        return Decision.allow(remaining);
    }

    @Override
    public synchronized void commit() {
        for (Limiter l : children) synchronized (l) { l.commit(); }
    }
}
