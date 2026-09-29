package ch15.virtual;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/** Virtual threads (Java 21, JEP 444): creation, identity, and 10,000 blocking tasks. */
public class VirtualThreadsDemo {
    static final int TASKS = 10_000;
    static final Duration WORK = Duration.ofMillis(100);      // stands in for a database or HTTP call

    public static void main(String[] args) throws Exception {
        // 1) Ways to create virtual threads
        Thread v1 = Thread.ofVirtual().name("vt-1").start(() -> System.out.println("1 hello from " + Thread.currentThread().getName()));
        v1.join();
        Thread v2 = Thread.startVirtualThread(() -> {});
        v2.join();
        System.out.println("1 isVirtual=" + v1.isVirtual() + ", daemon=" + v1.isDaemon() + ", main isVirtual=" + Thread.currentThread().isVirtual());

        // 2) A virtual thread runs on a carrier (platform) thread; toString shows it while mounted
        Thread.ofVirtual().start(() -> System.out.println("2 " + Thread.currentThread()
                .toString().replaceAll("#\\d+", "#N").replaceAll("worker-\\d+", "worker-K") + "   [ids vary]")).join();

        // 3) The headline demo: 10,000 tasks that each block for 100 ms
        long pooled = time(Executors.newFixedThreadPool(200));
        long virtual = time(Executors.newVirtualThreadPerTaskExecutor());
        System.out.printf("3 fixed pool of 200 platform threads: %,d ms  (theory: 10,000 / 200 x 100 ms = 5,000)   [machine-dependent]%n", pooled);
        System.out.printf("3 one virtual thread per task:        %,d ms  (theory: ~100 ms + overhead)             [machine-dependent]%n", virtual);

        // 4) How many carrier threads did 10,000 virtual threads actually use?
        Set<String> carriers = ConcurrentHashMap.newKeySet();
        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, TASKS).forEach(i -> ex.submit(() -> {
                String s = Thread.currentThread().toString();       // "VirtualThread[#..]/runnable@ForkJoinPool-1-worker-3"
                carriers.add(s.substring(s.indexOf('@') + 1));
                Thread.sleep(WORK);
                return null;
            }));
        }
        System.out.println("4 carrier threads used: " + carriers.size() + " <= cores ("
                + Runtime.getRuntime().availableProcessors() + ")? " + (carriers.size() <= Runtime.getRuntime().availableProcessors())
                + "   [machine-dependent]");

        // 5) Limiting concurrency with virtual threads: a Semaphore, not a small pool
        Semaphore dbPermits = new Semaphore(10);
        int[] maxInside = {0}; int[] inside = {0};
        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 200; i++) ex.submit(() -> {
                dbPermits.acquire();
                try {
                    synchronized (maxInside) { inside[0]++; maxInside[0] = Math.max(maxInside[0], inside[0]); }
                    Thread.sleep(10);
                    synchronized (maxInside) { inside[0]--; }
                } finally { dbPermits.release(); }
                return null;
            });
        }
        System.out.println("5 200 virtual threads, Semaphore(10): max concurrent 'DB calls' = " + maxInside[0]);
    }

    static long time(ExecutorService executor) {
        long t0 = System.nanoTime();
        try (executor) {                                          // close() waits for all tasks
            for (int i = 0; i < TASKS; i++) executor.submit(() -> { Thread.sleep(WORK); return null; });
        }
        return (System.nanoTime() - t0) / 1_000_000;
    }
}
