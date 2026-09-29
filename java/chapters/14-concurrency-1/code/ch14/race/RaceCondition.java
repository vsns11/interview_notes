package ch14.race;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/** 4 threads x 1,000,000 increments. Expected total: 4,000,000. */
public class RaceCondition {
    static final int THREADS = 4, PER_THREAD = 1_000_000;

    static int unsafeCount = 0;                               // shared, unprotected
    static int syncCount = 0;
    static final Object LOCK = new Object();
    static final AtomicInteger atomicCount = new AtomicInteger();
    static final LongAdder adder = new LongAdder();

    public static void main(String[] args) throws InterruptedException {
        run(() -> { for (int i = 0; i < PER_THREAD; i++) unsafeCount++; });
        System.out.printf("unsynchronized count++ : %,d   [nondeterministic - lost updates, changes every run]%n", unsafeCount);

        run(() -> { for (int i = 0; i < PER_THREAD; i++) synchronized (LOCK) { syncCount++; } });
        System.out.printf("synchronized           : %,d%n", syncCount);

        run(() -> { for (int i = 0; i < PER_THREAD; i++) atomicCount.incrementAndGet(); });
        System.out.printf("AtomicInteger          : %,d%n", atomicCount.get());

        run(() -> { for (int i = 0; i < PER_THREAD; i++) adder.increment(); });
        System.out.printf("LongAdder              : %,d%n", adder.sum());

        // Check-then-act race: two threads both see a free seat and both book it
        for (int attempt = 1; attempt <= 3; attempt++) {
            Seat seat = new Seat();
            Thread x = new Thread(() -> seat.bookUnsafe("ana"));
            Thread y = new Thread(() -> seat.bookUnsafe("raj"));
            x.start(); y.start(); x.join(); y.join();
            System.out.println("unsafe booking attempt " + attempt + ": successful bookings = " + seat.successes
                    + "   [nondeterministic - 2 means double-booked]");
        }
        Seat safe = new Seat();
        Thread x = new Thread(() -> safe.bookSafe("ana"));
        Thread y = new Thread(() -> safe.bookSafe("raj"));
        x.start(); y.start(); x.join(); y.join();
        System.out.println("synchronized booking: successful bookings = " + safe.successes);
    }

    static class Seat {
        String owner;
        int successes;
        void bookUnsafe(String who) {
            if (owner == null) {                              // check ...
                sleepQuietly();                               // (widen the window, as real work would)
                owner = who;                                  // ... then act
                successes++;
            }
        }
        synchronized void bookSafe(String who) {             // check and act under one lock
            if (owner == null) { sleepQuietly(); owner = who; successes++; }
        }
    }

    static void sleepQuietly() {
        try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static void run(Runnable body) throws InterruptedException {
        Thread[] ts = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) (ts[i] = new Thread(body)).start();
        for (Thread t : ts) t.join();
    }
}
