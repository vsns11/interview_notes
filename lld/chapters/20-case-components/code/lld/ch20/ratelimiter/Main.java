package lld.ch20.ratelimiter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    static final AtomicLong NOW = new AtomicLong();          // manual clock in nanoseconds
    static final Ticker TICKER = NOW::get;

    static void at(double seconds) { NOW.set((long) (seconds * 1e9)); }

    public static void main(String[] args) throws Exception {
        RateLimiterService svc = new RateLimiterService(
                List.of(new LimitRule(Algorithm.FIXED_WINDOW, 100, Duration.ofMinutes(1))), TICKER);
        svc.addRule("/search", Tier.FREE, new LimitRule(Algorithm.TOKEN_BUCKET, 3, Duration.ofSeconds(3))); // 1/s, burst 3
        svc.addRule("/search", Tier.PREMIUM, new LimitRule(Algorithm.TOKEN_BUCKET, 10, Duration.ofSeconds(1)));
        svc.addRule("/login", Tier.FREE,
                new LimitRule(Algorithm.SLIDING_LOG, 2, Duration.ofSeconds(10)),
                new LimitRule(Algorithm.SLIDING_LOG, 3, Duration.ofMinutes(1)));    // composite
        svc.setTier("pat", Tier.PREMIUM);

        System.out.println("== 1. Per-user, per-API token bucket (FREE: burst 3, 1 token/s)");
        at(0);
        for (int i = 1; i <= 5; i++) System.out.println("  t=0.0 ann /search #" + i + " -> " + show(svc.check("ann", "/search")));
        at(1.0);
        System.out.println("  t=1.0 ann /search    -> " + show(svc.check("ann", "/search")));
        System.out.println("  t=1.0 bob /search    -> " + show(svc.check("bob", "/search")) + "  (own bucket)");
        int premium = 0;
        for (int i = 0; i < 12; i++) if (svc.check("pat", "/search").allowed()) premium++;
        System.out.println("  t=1.0 pat (PREMIUM) 12 calls -> " + premium + " allowed");
        System.out.println("  t=1.0 ann /orders (default rule) -> " + show(svc.check("ann", "/orders")));

        System.out.println("== 2. Composite rule on /login: 2 per 10 s AND 3 per minute");
        double[] times = {0, 1, 2, 11, 12, 61};
        at(100);
        for (double t : times) {
            at(100 + t);
            System.out.println("  t=+" + (int) t + "s ann /login -> " + show(svc.check("ann", "/login")));
        }

        System.out.println("== 3. A denied request uses up nothing: 3 per minute AND 2 per 10 s (minute rule first)");
        Limiter login = LimiterFactory.create(List.of(
                new LimitRule(Algorithm.SLIDING_LOG, 3, Duration.ofMinutes(1)),
                new LimitRule(Algorithm.SLIDING_LOG, 2, Duration.ofSeconds(10))), TICKER);
        for (double t : new double[]{0, 1, 2, 11}) {
            at(200 + t);
            System.out.println("  t=+" + (int) t + "s -> " + show(login.tryAcquire()));
        }

        System.out.println("== 4. Boundary burst: limit 5 per 10 s, 5 calls at t=9.9 and 5 at t=10.1");
        for (Algorithm a : List.of(Algorithm.FIXED_WINDOW, Algorithm.SLIDING_LOG, Algorithm.SLIDING_COUNTER)) {
            Limiter l = LimiterFactory.create(new LimitRule(a, 5, Duration.ofSeconds(10)), TICKER);
            int allowed = 0;
            at(1009.9);
            for (int i = 0; i < 5; i++) if (l.tryAcquire().allowed()) allowed++;
            at(1010.1);
            for (int i = 0; i < 5; i++) if (l.tryAcquire().allowed()) allowed++;
            System.out.printf("  %-16s allowed %d of 10 within 0.2 s%n", a, allowed);
        }

        System.out.println("== 5. 64 threads x 10 calls on one bucket (capacity 50, no refill)");
        Limiter bucket = new TokenBucket(50, 0, TICKER);
        AtomicInteger ok = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(64);
        List<java.util.concurrent.Future<?>> fs = new ArrayList<>();
        for (int t = 0; t < 64; t++)
            fs.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < 10; i++) if (bucket.tryAcquire().allowed()) ok.incrementAndGet();
                return null;
            }));
        start.countDown();
        for (var f : fs) f.get();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("  allowed = " + ok.get() + " (exactly 50, never more)");
    }

    private static String show(Decision d) {
        return d.allowed() ? "200 OK (remaining " + d.remaining() + ")"
                : "429 Too Many Requests (retry after " + d.retryAfter().toMillis() + " ms)";
    }
}
