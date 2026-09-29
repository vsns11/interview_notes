package ch15.liveness;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** Create a real deadlock, detect it, then fix it two ways. */
public class DeadlockDemo {

    static class Account {
        final int id; int balance;
        final ReentrantLock lock = new ReentrantLock();
        Account(int id, int balance) { this.id = id; this.balance = balance; }
    }

    // BAD: locks in argument order. transfer(a,b) and transfer(b,a) at once -> each holds one lock, wants the other.
    static void transferBad(Account from, Account to, int amt, CountDownLatch bothHoldFirstLock) {
        synchronized (from) {
            bothHoldFirstLock.countDown();
            try { bothHoldFirstLock.await(); } catch (InterruptedException e) { return; }   // make the deadlock certain
            synchronized (to) { from.balance -= amt; to.balance += amt; }
        }
    }

    // FIX 1: global lock ordering (always lock the lower id first) -> no cycle possible
    static void transferOrdered(Account from, Account to, int amt) {
        Account first = from.id < to.id ? from : to, second = first == from ? to : from;
        synchronized (first) { synchronized (second) { from.balance -= amt; to.balance += amt; } }
    }

    // FIX 2: tryLock with timeout + back off and retry -> never waits forever
    static boolean transferTry(Account from, Account to, int amt) throws InterruptedException {
        while (true) {
            if (from.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                try {
                    if (to.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                        try { from.balance -= amt; to.balance += amt; return true; }
                        finally { to.lock.unlock(); }
                    }
                } finally { from.lock.unlock(); }
            }
            Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));   // random back-off avoids livelock
        }
    }

    public static void main(String[] args) throws Exception {
        Account a = new Account(1, 100), b = new Account(2, 100);
        CountDownLatch latch = new CountDownLatch(2);
        Thread t1 = new Thread(() -> transferBad(a, b, 10, latch), "transfer-a-to-b");
        Thread t2 = new Thread(() -> transferBad(b, a, 20, latch), "transfer-b-to-a");
        t1.setDaemon(true); t2.setDaemon(true);                  // so the JVM can still exit
        t1.start(); t2.start();

        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids;
        while ((ids = mx.findDeadlockedThreads()) == null) Thread.sleep(10);
        System.out.println("deadlock detected between " + ids.length + " threads:");
        ThreadInfo[] infos = mx.getThreadInfo(ids);
        Arrays.sort(infos, (x, y) -> x.getThreadName().compareTo(y.getThreadName()));
        for (ThreadInfo info : infos) {
            System.out.println("  " + info.getThreadName() + " is " + info.getThreadState()
                    + ", waiting for a lock held by " + info.getLockOwnerName());
        }
        System.out.println("t1 alive=" + t1.isAlive() + ", t2 alive=" + t2.isAlive() + " (stuck forever; only a restart helps)");

        // Fixes: 1,000 opposite-direction transfers each way, concurrently
        Account c = new Account(1, 1_000), d = new Account(2, 1_000);
        runBoth(() -> { for (int i = 0; i < 1_000; i++) transferOrdered(c, d, 1); },
                () -> { for (int i = 0; i < 1_000; i++) transferOrdered(d, c, 1); });
        System.out.println("lock ordering: finished, total money = " + (c.balance + d.balance));

        Account e = new Account(1, 1_000), f = new Account(2, 1_000);
        runBoth(() -> { for (int i = 0; i < 1_000; i++) try { transferTry(e, f, 1); } catch (InterruptedException x) { return; } },
                () -> { for (int i = 0; i < 1_000; i++) try { transferTry(f, e, 1); } catch (InterruptedException x) { return; } });
        System.out.println("tryLock + back-off: finished, total money = " + (e.balance + f.balance));
    }

    static void runBoth(Runnable r1, Runnable r2) throws InterruptedException {
        Thread x = new Thread(r1), y = new Thread(r2);
        x.start(); y.start(); x.join(); y.join();
    }
}
