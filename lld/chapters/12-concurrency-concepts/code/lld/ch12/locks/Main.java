package lld.ch12.locks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/** synchronized vs ReentrantLock, reentrancy, tryLock, ReadWriteLock, StampedLock. */
public class Main {

    // ---------- Reentrancy ----------
    static class Account {
        private final ReentrantLock lock = new ReentrantLock();
        private int balance = 100;

        void withdraw(int amount) {
            lock.lock();
            try {
                if (canWithdraw(amount)) balance -= amount;   // calls another locked method
            } finally {
                lock.unlock();                                // ALWAYS unlock in finally
            }
        }
        boolean canWithdraw(int amount) {
            lock.lock();                                      // same thread re-acquires: no self-deadlock
            try {
                System.out.println("hold count inside nested call = " + lock.getHoldCount());
                return balance >= amount;
            } finally {
                lock.unlock();
            }
        }
        int balance() { lock.lock(); try { return balance; } finally { lock.unlock(); } }
    }

    // ---------- Read-write lock: many readers OR one writer ----------
    static class ConfigStore {
        private final Map<String, String> map = new HashMap<>();
        private final ReadWriteLock rw = new ReentrantReadWriteLock();
        private final Lock r = rw.readLock(), w = rw.writeLock();

        String get(String k) { r.lock(); try { return map.get(k); } finally { r.unlock(); } }
        void put(String k, String v) { w.lock(); try { map.put(k, v); } finally { w.unlock(); } }

        /** Used by the demo: prove several readers are inside the read lock at once. */
        <T> T readWhile(java.util.concurrent.Callable<T> action) throws Exception {
            r.lock(); try { return action.call(); } finally { r.unlock(); }
        }
    }

    // ---------- StampedLock optimistic read ----------
    static class Point {
        private final StampedLock sl = new StampedLock();
        private double x, y;

        void move(double dx, double dy) {
            long stamp = sl.writeLock();
            try { x += dx; y += dy; } finally { sl.unlockWrite(stamp); }
        }
        double distanceFromOrigin() {
            long stamp = sl.tryOptimisticRead();   // no locking at all, just a version number
            double cx = x, cy = y;                 // copy fields to locals
            if (!sl.validate(stamp)) {             // a writer slipped in: fall back to a real read lock
                stamp = sl.readLock();
                try { cx = x; cy = y; } finally { sl.unlockRead(stamp); }
            }
            return Math.hypot(cx, cy);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Reentrancy ===");
        Account acc = new Account();
        acc.withdraw(30);
        System.out.println("balance = " + acc.balance());

        System.out.println("=== tryLock with timeout ===");
        ReentrantLock printer = new ReentrantLock();
        CountDownLatch mainHolds = new CountDownLatch(1);
        printer.lock();                           // main grabs the printer and keeps it
        try {
            Thread impatient = new Thread(() -> {
                try {
                    mainHolds.await();
                    if (printer.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try { System.out.println("worker: got the printer"); } finally { printer.unlock(); }
                    } else {
                        System.out.println("worker: printer busy for 100 ms, doing something else instead");
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            impatient.start();
            mainHolds.countDown();
            impatient.join();
        } finally {
            printer.unlock();
        }
        System.out.println("tryLock() now (free)  = " + printer.tryLock());
        printer.unlock();

        System.out.println("=== ReadWriteLock: readers share ===");
        ConfigStore store = new ConfigStore();
        store.put("mode", "fast");
        int readers = 3;
        CyclicBarrier allReadersInside = new CyclicBarrier(readers);
        Thread[] rs = new Thread[readers];
        boolean[] ok = new boolean[readers];
        for (int i = 0; i < readers; i++) {
            final int id = i;
            rs[i] = new Thread(() -> {
                try {
                    ok[id] = store.readWhile(() -> {
                        // If the read lock were exclusive, the barrier would time out.
                        allReadersInside.await(2, TimeUnit.SECONDS);
                        return "fast".equals(store.get("mode"));   // read lock is reentrant too
                    });
                } catch (TimeoutException | BrokenBarrierException e) {
                    ok[id] = false;
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            rs[i].start();
        }
        for (Thread t : rs) t.join();
        System.out.println(readers + " readers held the read lock at the same time: " + (ok[0] && ok[1] && ok[2]));

        System.out.println("=== StampedLock optimistic read ===");
        Point p = new Point();
        p.move(3, 4);
        System.out.println("distance = " + p.distanceFromOrigin());
    }
}
