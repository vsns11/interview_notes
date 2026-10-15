package ch22.tokenbucket;

import java.util.concurrent.TimeUnit;

/**
 * Blocking token bucket with LAZY refill: no background thread.
 * The bucket remembers when it last refilled and, on every call, adds the tokens
 * that "would have dripped in" since then. getToken() blocks until a token exists.
 */
public final class LazyRefillBucket implements TokenBucket {
    private final int capacity;              // maximum burst
    private final long nanosPerToken;        // one token drips in every nanosPerToken
    private long tokens;                     // guarded by 'this'
    private long lastRefillNanos;            // guarded by 'this'

    public LazyRefillBucket(int capacity, int tokensPerSecond) {
        if (capacity < 1 || tokensPerSecond < 1) throw new IllegalArgumentException();
        this.capacity = capacity;
        this.nanosPerToken = TimeUnit.SECONDS.toNanos(1) / tokensPerSecond;
        this.tokens = capacity;              // start full: a fresh client may burst
        this.lastRefillNanos = System.nanoTime();
    }

    /** Adds whole tokens earned since lastRefillNanos. Caller must hold the lock. */
    private void refill() {
        long now = System.nanoTime();
        if (tokens == capacity) {                          // a full bucket earns nothing: idle time
            lastRefillNanos = now;                         // is not "banked" toward the next token
            return;
        }
        long earned = (now - lastRefillNanos) / nanosPerToken;
        if (earned > 0) {
            tokens = Math.min(capacity, tokens + earned);
            lastRefillNanos += earned * nanosPerToken;     // keep the fractional remainder
            if (tokens == capacity) lastRefillNanos = now;
        }
    }

    @Override
    public synchronized void getToken() throws InterruptedException {
        refill();
        while (tokens == 0) {
            // How long until the next token? Wait at least that long, releasing the lock meanwhile.
            long waitNanos = nanosPerToken - (System.nanoTime() - lastRefillNanos);
            if (waitNanos > 0) {
                TimeUnit.NANOSECONDS.timedWait(this, waitNanos);  // = wait(ms, ns); releases the monitor
            }
            refill();                       // re-check after waking: someone else may have taken it
        }
        tokens--;
    }

    @Override
    public synchronized boolean tryGetToken() {
        refill();
        if (tokens == 0) return false;
        tokens--;
        return true;
    }
}
