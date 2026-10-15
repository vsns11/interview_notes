package ch14.threads;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Preemptive (OS-scheduled platform threads) vs cooperative (virtual threads on ONE carrier).
 * Every thread spins until a shared deadline and counts its loop iterations.
 * A thread whose count is still 0 never got the CPU before the deadline.
 */
public class Multitasking {
    static final int N = 3;

    public static void main(String[] args) throws Exception {
        // Must be set before the first virtual thread is created: the scheduler reads it once.
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "1");
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "1");

        int cores = Runtime.getRuntime().availableProcessors();
        int platformThreads = cores * 3;                         // more runnable threads than cores
        System.out.println("platform threads that ran before the deadline: "
                + race(platformThreads, false, false) + " of " + platformThreads);
        System.out.println("virtual threads, 1 carrier, no yield:          "
                + race(N, true, false) + " of " + N);
        System.out.println("virtual threads, 1 carrier, Thread.yield():    "
                + race(N, true, true) + " of " + N);
    }

    /** Starts n spinning threads; returns how many did at least one iteration before the deadline. */
    static int race(int n, boolean virtual, boolean yield) throws InterruptedException {
        long deadline = System.nanoTime() + 300_000_000L;      // 300 ms from now
        AtomicLongArray loops = new AtomicLongArray(n);
        Thread[] ts = new Thread[n];
        for (int i = 0; i < n; i++) {
            int id = i;
            Runnable spin = () -> {
                long count = 0;
                while (System.nanoTime() < deadline) {         // pure CPU work, never blocks
                    count++;
                    if (yield && count % 1_000 == 0) Thread.yield();   // a voluntary yield point
                }
                loops.set(id, count);
            };
            ts[i] = virtual ? Thread.ofVirtual().unstarted(spin) : Thread.ofPlatform().unstarted(spin);
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        int ran = 0;
        for (int i = 0; i < n; i++) if (loops.get(i) > 0) ran++;
        return ran;
    }
}
