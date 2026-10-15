package ch22.asynctosync;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A stand-in for a callback-based library we cannot change (an HTTP client, a message
 * broker driver...). fetch() returns at once; the result arrives later through the
 * callback, on the library's own single "io" thread. The key picks the behaviour so
 * the demo can exercise every edge case:
 *   "slow..."   success after 150 ms        "fail"   onFailure after 50 ms
 *   "cached"    success delivered SYNCHRONOUSLY, on the caller's thread, before fetch() returns
 *   "never"     the callback is never called "twice"  onSuccess called two times
 *   "k..."      success after 20 ms (used for many concurrent callers)
 */
public final class AsyncService implements AutoCloseable {

    public interface Callback<T> {
        void onSuccess(T value);

        void onFailure(Throwable error);
    }

    private final ScheduledExecutorService io = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "io-thread");
        t.setDaemon(true);
        return t;
    });

    public void fetch(String key, Callback<String> cb) {
        switch (key) {
            case "cached" -> cb.onSuccess("value-of-cached");            // caller's thread, right now
            case "never" -> { /* lost request: no callback, ever */ }
            case "fail" -> io.schedule(() -> cb.onFailure(new IOException("connection reset")), 50, TimeUnit.MILLISECONDS);
            case "twice" -> io.schedule(() -> {
                cb.onSuccess("first");
                cb.onSuccess("second");                                   // buggy library: duplicate delivery
            }, 50, TimeUnit.MILLISECONDS);
            default -> {
                long delay = key.startsWith("slow") ? 150 : 20;
                io.schedule(() -> cb.onSuccess("value-of-" + key), delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    /** Lets the demo run code ON the io thread, the way a user callback would. */
    public void runOnIoThread(Runnable r) {
        io.execute(r);
    }

    @Override
    public void close() {
        io.shutdownNow();
    }
}
