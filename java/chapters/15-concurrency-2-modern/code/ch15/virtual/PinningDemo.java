package ch15.virtual;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Blocking INSIDE synchronized. On Java 21-23 each such virtual thread pins its carrier;
 * since Java 24 (JEP 491) it unmounts normally. Uses only Java 21 APIs so it can run on both.
 */
public class PinningDemo {
    public static void main(String[] args) {
        int tasks = 1_000;
        int carriers = Runtime.getRuntime().availableProcessors();
        long t0 = System.nanoTime();
        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < tasks; i++) {
                Object lock = new Object();                   // a separate lock per task: no contention at all
                ex.submit(() -> {
                    synchronized (lock) {
                        Thread.sleep(50);                     // blocking while holding a monitor
                    }
                    return null;
                });
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("Java " + Runtime.version().feature() + ": " + tasks + " virtual threads sleeping 50 ms inside synchronized took "
                + ms + " ms   [machine-dependent]");
        System.out.println("if every carrier were pinned: ~" + (tasks / carriers + 1) * 50 + " ms with " + carriers + " carriers");
    }
}
