package ch22.tokenbucket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Capacity 3, refill 10 tokens/second (one token per 100 ms).
 * 8 threads ask for a token at the same moment: 3 should get one at once,
 * the other 5 should be released roughly 100 ms apart.
 */
public class Main {

    static long[] demand(TokenBucket bucket, int callers) throws InterruptedException {
        long[] grantedAtMs = new long[callers];
        CountDownLatch go = new CountDownLatch(1);       // start all callers at the same instant
        List<Thread> threads = new ArrayList<>();
        long[] t0 = new long[1];
        for (int i = 0; i < callers; i++) {
            int id = i;
            Thread t = new Thread(() -> {
                try {
                    go.await();
                    bucket.getToken();
                    grantedAtMs[id] = (System.nanoTime() - t0[0]) / 1_000_000;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "caller-" + i);
            threads.add(t);
            t.start();
        }
        t0[0] = System.nanoTime();
        go.countDown();
        for (Thread t : threads) t.join();               // join makes grantedAtMs[] visible to main
        Arrays.sort(grantedAtMs);
        return grantedAtMs;
    }

    static String rounded(long[] ms) {                   // round to the nearest 100 ms to hide jitter
        long[] r = new long[ms.length];
        for (int i = 0; i < ms.length; i++) r[i] = Math.round(ms[i] / 100.0) * 100;
        return Arrays.toString(r);
    }

    static void report(String name, TokenBucket bucket) throws InterruptedException {
        long[] ms = demand(bucket, 8);
        System.out.println(name);
        System.out.println("  grant times, rounded to 100 ms: " + rounded(ms));
        System.out.println("  first 3 within 50 ms (burst):    " + (ms[2] < 50));
        System.out.println("  8th token not before ~500 ms:    " + (ms[7] >= 450));
        System.out.println("  tryGetToken() right after:       " + bucket.tryGetToken());
    }

    public static void main(String[] args) throws InterruptedException {
        report("LazyRefillBucket (no extra thread)", new LazyRefillBucket(3, 10));

        BackgroundRefillBucket bg = BackgroundRefillBucket.start(3, 10);
        report("BackgroundRefillBucket (daemon refiller)", bg);
        Thread.sleep(350);                               // idle: the refiller tops the bucket up
        int available = 0;
        while (bg.tryGetToken()) available++;
        System.out.println("  after 350 ms idle, tokens available: " + available + " (capped at capacity 3)");
        bg.close();
        System.out.println("  refiller alive after close(): " + bg.refillerAlive());
    }
}
