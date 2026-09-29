package lld.ch13.ratelimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/** Thread-safe token-bucket rate limiter: a lock-based version, a lock-free version, and per-user limits. */
public class Main {

    @FunctionalInterface interface Ticker { long nanos(); }

    static final class FakeTicker implements Ticker {
        private final AtomicLong now = new AtomicLong();
        public long nanos() { return now.get(); }
        void advance(long amount, TimeUnit unit) { now.addAndGet(unit.toNanos(amount)); }
    }

    interface RateLimiter { boolean tryAcquire(); }

    /**
     * Bucket holds up to 'capacity' tokens (the allowed burst). Tokens drip in at 'refillPerSecond'.
     * Each request takes one token; no token -> rejected. Refill is computed lazily from elapsed time.
     */
    static final class TokenBucket implements RateLimiter {
        private final double capacity, tokensPerNano;
        private final Ticker ticker;
        private double tokens;          // guarded by 'this'
        private long lastRefill;        // guarded by 'this'

        TokenBucket(int capacity, double refillPerSecond, Ticker ticker) {
            this.capacity = capacity;
            this.tokensPerNano = refillPerSecond / 1e9;
            this.ticker = ticker;
            this.tokens = capacity;     // start full
            this.lastRefill = ticker.nanos();
        }

        public synchronized boolean tryAcquire() {
            long now = ticker.nanos();
            tokens = Math.min(capacity, tokens + (now - lastRefill) * tokensPerNano);   // refill + acquire are ONE atomic step
            lastRefill = now;
            if (tokens >= 1) { tokens -= 1; return true; }
            return false;
        }
    }

    /** Same algorithm, lock-free: the whole state is one immutable record swapped with CAS. */
    static final class LockFreeTokenBucket implements RateLimiter {
        private record State(double tokens, long lastRefill) { }
        private final double capacity, tokensPerNano;
        private final Ticker ticker;
        private final AtomicReference<State> state;

        LockFreeTokenBucket(int capacity, double refillPerSecond, Ticker ticker) {
            this.capacity = capacity;
            this.tokensPerNano = refillPerSecond / 1e9;
            this.ticker = ticker;
            this.state = new AtomicReference<>(new State(capacity, ticker.nanos()));
        }

        public boolean tryAcquire() {
            while (true) {
                State cur = state.get();
                long now = Math.max(ticker.nanos(), cur.lastRefill());   // never move time backwards
                double refilled = Math.min(capacity, cur.tokens() + (now - cur.lastRefill()) * tokensPerNano);
                boolean ok = refilled >= 1;
                State next = new State(ok ? refilled - 1 : refilled, now);
                if (state.compareAndSet(cur, next)) return ok;            // lost the race? recompute and retry
            }
        }
    }

    /** One bucket per client key (user id, API key, IP). computeIfAbsent creates each bucket exactly once. */
    static final class PerClientLimiter {
        private final ConcurrentHashMap<String, RateLimiter> buckets = new ConcurrentHashMap<>();
        private final Supplier<RateLimiter> factory;
        PerClientLimiter(Supplier<RateLimiter> factory) { this.factory = factory; }
        boolean allow(String clientId) { return buckets.computeIfAbsent(clientId, k -> factory.get()).tryAcquire(); }
    }

    static String burst(RateLimiter rl, int requests) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < requests; i++) sb.append(rl.tryAcquire() ? 'Y' : 'n');
        return sb.toString();
    }

    static int concurrentBurst(RateLimiter rl, int threads) throws InterruptedException {
        AtomicInteger allowed = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> {
                try { go.await(); } catch (InterruptedException e) { return; }
                if (rl.tryAcquire()) allowed.incrementAndGet();
            });
            ts.add(t); t.start();
        }
        go.countDown();
        for (Thread t : ts) t.join();
        return allowed.get();
    }

    public static void main(String[] args) throws InterruptedException {
        for (String kind : List.of("synchronized", "lock-free")) {
            FakeTicker clock = new FakeTicker();
            RateLimiter rl = kind.equals("synchronized") ? new TokenBucket(5, 2, clock) : new LockFreeTokenBucket(5, 2, clock);
            System.out.println("[" + kind + "] capacity 5, refill 2/s");
            System.out.println("  t=0.0s  7 requests : " + burst(rl, 7));
            clock.advance(1, TimeUnit.SECONDS);
            System.out.println("  t=1.0s  3 requests : " + burst(rl, 3));
            clock.advance(250, TimeUnit.MILLISECONDS);
            System.out.println("  t=1.25s 1 request  : " + burst(rl, 1) + "  (only 0.5 token so far)");
            clock.advance(250, TimeUnit.MILLISECONDS);
            System.out.println("  t=1.5s  1 request  : " + burst(rl, 1));
            clock.advance(1, TimeUnit.HOURS);
            System.out.println("  after 1 idle hour  : allowed " + concurrentBurst(rl, 100) + " of 100 concurrent requests (burst capped at 5)");
        }

        FakeTicker clock = new FakeTicker();
        PerClientLimiter api = new PerClientLimiter(() -> new TokenBucket(3, 1, clock));
        StringBuilder alice = new StringBuilder(), bob = new StringBuilder();
        for (int i = 0; i < 4; i++) { alice.append(api.allow("alice") ? 'Y' : 'n'); }
        for (int i = 0; i < 2; i++) { bob.append(api.allow("bob") ? 'Y' : 'n'); }
        System.out.println("per-client (3 burst): alice " + alice + ", bob " + bob + "  (alice's spam doesn't hurt bob)");
    }
}
