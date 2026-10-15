package ch22.asynctosync;

import ch22.asynctosync.SyncWrappers.SyncFetcher;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final Duration TIMEOUT = Duration.ofMillis(300);

    public static void main(String[] args) throws Exception {
        try (AsyncService service = new AsyncService()) {
            List<SyncFetcher> wrappers = List.of(
                    new SyncWrappers.MonitorSync(service), new SyncWrappers.ConditionSync(service),
                    new SyncWrappers.LatchSync(service), new SyncWrappers.FutureSync(service));

            System.out.println("== 1. every wrapper against every edge case (timeout " + TIMEOUT.toMillis() + " ms) ==");
            for (String key : List.of("slow", "fail", "cached", "never", "twice")) {
                List<String> results = new ArrayList<>();
                for (SyncFetcher w : wrappers) results.add(attempt(w, key));
                boolean allSame = results.stream().distinct().count() == 1;
                System.out.printf("1 %-7s -> %s%n", key, allSame ? results.get(0) + "   (same for all 4 wrappers)" : results);
            }
            System.out.println();
            System.out.println("== 2. 50 threads call the latch wrapper at once, each with its own key ==");
            concurrentCallers(new SyncWrappers.LatchSync(service));

            System.out.println();
            System.out.println("== 3. interrupting a caller that is blocked waiting ==");
            interruptCaller(new SyncWrappers.ConditionSync(service));

            System.out.println();
            System.out.println("== 4. calling the blocking wrapper FROM a callback on the io thread ==");
            blockingOnTheCallbackThread(service, new SyncWrappers.FutureSync(service));

            System.out.println();
            SyncWrappers.FutureSync futures = new SyncWrappers.FutureSync(service);
            String composed = futures.fetchAsync("k1").thenCombine(futures.fetchAsync("k2"), (a, b) -> a + "+" + b).join();
            System.out.println("5 no blocking needed when the caller can compose: " + composed);
        }
    }

    static String attempt(SyncFetcher w, String key) {
        try {
            return w.fetch(key, TIMEOUT);
        } catch (ExecutionException e) {
            return "ExecutionException(" + e.getCause().getMessage() + ")";
        } catch (TimeoutException e) {
            return "TimeoutException";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "interrupted";
        }
    }

    static void concurrentCallers(SyncFetcher w) throws InterruptedException {
        AtomicInteger correct = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        List<Thread> callers = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final String key = "k" + i;
            callers.add(Thread.ofPlatform().start(() -> {
                try {
                    go.await();
                    if (("value-of-" + key).equals(w.fetch(key, TIMEOUT))) correct.incrementAndGet();
                } catch (Exception e) {
                    // counted as incorrect
                }
            }));
        }
        go.countDown();
        for (Thread t : callers) t.join();
        System.out.println("2 callers that got exactly their own result: " + correct.get() + " of 50");
    }

    static void interruptCaller(SyncFetcher w) throws InterruptedException {
        String[] outcome = {"?"};
        Thread caller = Thread.ofPlatform().start(() -> {
            try {
                w.fetch("never", Duration.ofSeconds(10));
                outcome[0] = "returned?!";
            } catch (InterruptedException e) {
                outcome[0] = "InterruptedException";
            } catch (Exception e) {
                outcome[0] = e.toString();
            }
        });
        Thread.sleep(100);
        long t0 = System.nanoTime();
        caller.interrupt();
        caller.join();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("3 blocked caller interrupted -> " + outcome[0] + ", left within 100 ms (not after the 10 s timeout): " + (ms < 100));
    }

    static void blockingOnTheCallbackThread(AsyncService service, SyncFetcher w) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        String[] outcome = {"?"};
        service.runOnIoThread(() -> {               // we are now ON the single io thread
            outcome[0] = attempt(w, "slow");          // its reply needs the io thread, which we are blocking
            done.countDown();
        });
        done.await();
        System.out.println("4 sync call from the io thread -> " + outcome[0] + " (would hang forever without a timeout)");
    }
}
