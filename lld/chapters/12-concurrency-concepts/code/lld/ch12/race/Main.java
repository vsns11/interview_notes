package lld.ch12.race;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** Race conditions, three fixes, and the visibility problem solved with volatile. */
public class Main {

    interface Counter { void increment(); int get(); }

    /** BROKEN: count++ is read-modify-write (3 steps), so concurrent increments get lost. */
    static class UnsafeCounter implements Counter {
        private int count;
        public void increment() { count++; }
        public int get() { return count; }
    }

    /** Fix 1: a mutex (the object's intrinsic lock) makes the critical section atomic. */
    static class SynchronizedCounter implements Counter {
        private int count;
        public synchronized void increment() { count++; }
        public synchronized int get() { return count; }   // reads need the lock too (visibility)
    }

    /** Fix 2: a lock-free atomic built on compare-and-swap. */
    static class AtomicCounter implements Counter {
        private final AtomicInteger count = new AtomicInteger();
        public void increment() { count.incrementAndGet(); }
        public int get() { return count.get(); }
    }

    static final int THREADS = 4, PER_THREAD = 250_000;

    static int hammer(Counter c) throws InterruptedException {
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> { for (int j = 0; j < PER_THREAD; j++) c.increment(); });
            ts.add(t);
            t.start();
        }
        for (Thread t : ts) t.join();   // join() also creates a happens-before edge: we see their writes
        return c.get();
    }

    // ---------- Visibility ----------
    static class Worker implements Runnable {
        private volatile boolean running = true;   // without volatile the loop may never see 'false'
        private long iterations;
        public void run() { while (running) iterations++; }
        void stop() { running = false; }
    }

    public static void main(String[] args) throws Exception {
        int expected = THREADS * PER_THREAD;
        System.out.println("expected                : " + expected);
        int unsafe = hammer(new UnsafeCounter());
        System.out.println("unsafe counter          : " + (unsafe == expected ? "correct this run (luck!)" : "LOST " + (expected - unsafe) + " updates"));
        System.out.println("synchronized counter    : " + hammer(new SynchronizedCounter()));
        System.out.println("atomic counter          : " + hammer(new AtomicCounter()));

        // Check-then-act race, shown safely: two threads both "see" an empty seat.
        // Here we fix it with a single atomic operation instead of check + act.
        AtomicInteger seatOwner = new AtomicInteger(0);          // 0 = free
        Thread a = new Thread(() -> seatOwner.compareAndSet(0, 1));
        Thread b = new Thread(() -> seatOwner.compareAndSet(0, 2));
        a.start(); b.start(); a.join(); b.join();
        System.out.println("seat has exactly one owner: " + (seatOwner.get() == 1 || seatOwner.get() == 2));

        Worker w = new Worker();
        Thread t = new Thread(w);
        t.start();
        Thread.sleep(50);
        w.stop();                       // volatile write ...
        t.join(2_000);                  // ... is guaranteed to become visible to the worker
        System.out.println("volatile flag stopped worker: " + !t.isAlive());
    }
}
