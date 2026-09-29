package lld.ch12.deadlock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** Create a real deadlock, detect it, then fix it two ways: lock ordering and tryLock + back-off. */
public class Main {

    static class Account {
        final int id;
        final ReentrantLock lock = new ReentrantLock();
        int balance;
        Account(int id, int balance) { this.id = id; this.balance = balance; }
    }

    // ---------- Fix 1: global lock ordering (always lock the smaller id first) ----------
    static void transferOrdered(Account from, Account to, int amount) {
        Account first = from.id < to.id ? from : to;
        Account second = from.id < to.id ? to : from;
        first.lock.lock();
        try {
            second.lock.lock();
            try {
                if (from.balance >= amount) { from.balance -= amount; to.balance += amount; }
            } finally { second.lock.unlock(); }
        } finally { first.lock.unlock(); }
    }

    // ---------- Fix 2: tryLock with timeout + random back-off (breaks "hold and wait") ----------
    static void transferTryLock(Account from, Account to, int amount) throws InterruptedException {
        while (true) {
            if (from.lock.tryLock(1, TimeUnit.MILLISECONDS)) {
                try {
                    if (to.lock.tryLock(1, TimeUnit.MILLISECONDS)) {
                        try {
                            if (from.balance >= amount) { from.balance -= amount; to.balance += amount; }
                            return;
                        } finally { to.lock.unlock(); }
                    }
                } finally { from.lock.unlock(); }         // give up what we hold
            }
            // Random back-off: without it, both threads could retry in lock-step forever (LIVELOCK).
            Thread.sleep(0, ThreadLocalRandom.current().nextInt(1, 100_000));
        }
    }

    public static void main(String[] args) throws Exception {
        demonstrateDeadlock();

        Account a = new Account(1, 1_000), b = new Account(2, 1_000);
        Thread t1 = new Thread(() -> { for (int i = 0; i < 50_000; i++) transferOrdered(a, b, 1); });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 50_000; i++) transferOrdered(b, a, 1); });
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("lock ordering: finished, total money = " + (a.balance + b.balance));

        Account c = new Account(3, 1_000), d = new Account(4, 1_000);
        Thread t3 = new Thread(() -> { try { for (int i = 0; i < 5_000; i++) transferTryLock(c, d, 1); } catch (InterruptedException ignored) { } });
        Thread t4 = new Thread(() -> { try { for (int i = 0; i < 5_000; i++) transferTryLock(d, c, 1); } catch (InterruptedException ignored) { } });
        t3.start(); t4.start(); t3.join(); t4.join();
        System.out.println("tryLock + back-off: finished, total money = " + (c.balance + d.balance));
    }

    /** Two threads lock in opposite order. A barrier forces the bad interleaving every time. */
    static void demonstrateDeadlock() throws Exception {
        Object lockA = new Object(), lockB = new Object();
        CyclicBarrier bothHoldOne = new CyclicBarrier(2);
        Thread x = new Thread(() -> {
            synchronized (lockA) { await(bothHoldOne); synchronized (lockB) { System.out.println("never printed"); } }
        }, "thread-X");
        Thread y = new Thread(() -> {
            synchronized (lockB) { await(bothHoldOne); synchronized (lockA) { System.out.println("never printed"); } }
        }, "thread-Y");
        x.setDaemon(true); y.setDaemon(true);   // daemon threads do not keep the JVM alive
        x.start(); y.start();

        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = null;
        for (int i = 0; i < 200 && ids == null; i++) { Thread.sleep(10); ids = mx.findDeadlockedThreads(); }
        String[] names = ids == null ? new String[0]
                : Arrays.stream(mx.getThreadInfo(ids)).map(ThreadInfo::getThreadName).sorted().toArray(String[]::new);
        System.out.println("deadlock detected between: " + Arrays.toString(names)
                + "  (states " + x.getState() + ", " + y.getState() + ")");
    }

    static void await(CyclicBarrier b) {
        try { b.await(); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
