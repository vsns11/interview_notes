import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryDemo {

    static class TransientException extends RuntimeException {
        TransientException(String m) { super(m); }
    }
    static class PermanentException extends RuntimeException {
        PermanentException(String m) { super(m); }
    }

    static <T> T executeWithRetry(Callable<T> task, int maxAttempts,
                                  long baseDelayMs, double factor,
                                  boolean useJitter) throws Exception {
        Random random = new Random();
        Exception last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = task.call();
                if (attempt > 1) System.out.println("      succeeded on attempt " + attempt);
                return result;
            } catch (PermanentException e) {
                System.out.println("      permanent error, NOT retrying: " + e.getMessage());
                throw e;
            } catch (Exception e) {
                last = e;
                if (attempt == maxAttempts) break;

                long delay = (long) (baseDelayMs * Math.pow(factor, attempt - 1));
                delay = Math.min(delay, 5000);
                if (useJitter) {
                    delay = delay / 2 + (long) (random.nextDouble() * delay / 2);
                }
                System.out.printf("      attempt %d failed (%s), waiting %dms%n",
                        attempt, e.getMessage(), delay);
                Thread.sleep(delay);
            }
        }
        throw last;
    }

    static class FlakyService {
        private final AtomicInteger calls = new AtomicInteger();
        private final int failuresBeforeSuccess;

        FlakyService(int n) { this.failuresBeforeSuccess = n; }

        String call() {
            int n = calls.incrementAndGet();
            if (n <= failuresBeforeSuccess) throw new TransientException("timeout");
            return "OK after " + n + " calls";
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("\n=== 1. Transient failure, recovers on 3rd attempt ===");
        FlakyService flaky = new FlakyService(2);
        System.out.println("      result: " +
                executeWithRetry(flaky::call, 5, 100, 2.0, false));

        System.out.println("\n=== 2. Permanent failure - should NOT retry ===");
        try {
            executeWithRetry(() -> { throw new PermanentException("HTTP 401"); },
                    5, 100, 2.0, false);
        } catch (PermanentException e) {
            System.out.println("      gave up immediately (correct)");
        }

        System.out.println("\n=== 3. Backoff schedule, no jitter ===");
        try {
            executeWithRetry(() -> { throw new TransientException("503"); },
                    5, 100, 2.0, false);
        } catch (Exception ignored) { }

        System.out.println("\n=== 4. Same schedule WITH jitter ===");
        try {
            executeWithRetry(() -> { throw new TransientException("503"); },
                    5, 100, 2.0, true);
        } catch (Exception ignored) { }

        System.out.println("\n=== 5. Thundering herd: 1000 clients retry at once ===");
        demonstrateHerd(false);
        demonstrateHerd(true);
    }

    static void demonstrateHerd(boolean useJitter) {
        Random random = new Random();
        Map<Long, Integer> buckets = new TreeMap<>();
        long baseDelay = 1000;

        for (int i = 0; i < 1000; i++) {
            long delay = baseDelay;
            if (useJitter) {
                delay = delay / 2 + (long) (random.nextDouble() * delay / 2);
            }
            buckets.merge(delay / 100 * 100, 1, Integer::sum);
        }

        int worst = buckets.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        System.out.printf("      jitter=%-5s -> busiest 100ms bucket holds %d of 1000 clients%n",
                useJitter, worst);
    }
}
