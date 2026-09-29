package ch14.locks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

/** ReentrantLock (tryLock, timeouts, interruptible, Condition) and ReentrantReadWriteLock. */
public class LocksDemo {
    public static void main(String[] args) throws Exception {
        ReentrantLock lock = new ReentrantLock();

        // 1) The standard idiom: lock(); try { ... } finally { unlock(); }
        lock.lock();
        try {
            System.out.println("holding lock: isHeldByCurrentThread=" + lock.isHeldByCurrentThread()
                    + " holdCount=" + lock.getHoldCount());
            lock.lock();                                          // reentrant
            try { System.out.println("re-entered: holdCount=" + lock.getHoldCount()); }
            finally { lock.unlock(); }
        } finally {
            lock.unlock();
        }

        // 2) tryLock with a timeout: give up instead of waiting forever
        CountDownLatch held = new CountDownLatch(1), release = new CountDownLatch(1);
        Thread holder = new Thread(() -> {
            lock.lock();
            try { held.countDown(); release.await(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { lock.unlock(); }
        });
        holder.start();
        held.await();
        long t0 = System.nanoTime();
        boolean got = lock.tryLock(150, TimeUnit.MILLISECONDS);
        System.out.println("tryLock(150ms) while another thread holds it -> " + got
                + " after >=150ms: " + ((System.nanoTime() - t0) >= 150_000_000L));

        // 3) lockInterruptibly: a waiting thread can be cancelled
        Thread waiter = new Thread(() -> {
            try { lock.lockInterruptibly(); lock.unlock(); System.out.println("never"); }
            catch (InterruptedException e) { System.out.println("waiter: interrupted while waiting for the lock"); }
        });
        waiter.start();
        while (!lock.hasQueuedThread(waiter)) Thread.sleep(1);
        waiter.interrupt();
        waiter.join();
        release.countDown();
        holder.join();

        // 4) Condition: like wait/notify but you can have several per lock
        Lock l = new ReentrantLock();
        Condition ready = l.newCondition();
        boolean[] flag = {false};
        Thread signaller = new Thread(() -> {
            l.lock();
            try { flag[0] = true; ready.signalAll(); } finally { l.unlock(); }
        });
        l.lock();
        try {
            signaller.start();
            while (!flag[0]) ready.await();                       // releases l while waiting
            System.out.println("Condition: woke up, flag = " + flag[0]);
        } finally { l.unlock(); }
        signaller.join();

        // 5) ReadWriteLock: 3 readers inside at the same time; a writer is exclusive
        ReadWriteLock rw = new ReentrantReadWriteLock();
        CyclicBarrier allThreeInside = new CyclicBarrier(3);      // trips only if 3 readers hold the read lock together
        AtomicInteger maxReaders = new AtomicInteger();
        Thread[] readers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            readers[i] = new Thread(() -> {
                rw.readLock().lock();
                try {
                    allThreeInside.await(2, TimeUnit.SECONDS);
                    maxReaders.accumulateAndGet(((ReentrantReadWriteLock) rw).getReadLockCount(), Math::max);
                } catch (Exception e) { System.out.println("readers could not share: " + e); }
                finally { rw.readLock().unlock(); }
            });
            readers[i].start();
        }
        for (Thread r : readers) r.join();
        System.out.println("ReadWriteLock: readers holding the read lock simultaneously = " + maxReaders.get());
        rw.writeLock().lock();
        try {
            System.out.println("writer holds write lock; can a reader get in now? " + tryReadFromOtherThread(rw));
        } finally { rw.writeLock().unlock(); }
    }

    static boolean tryReadFromOtherThread(ReadWriteLock rw) throws InterruptedException {
        boolean[] ok = {false};
        Thread t = new Thread(() -> { if (rw.readLock().tryLock()) { ok[0] = true; rw.readLock().unlock(); } });
        t.start(); t.join();
        return ok[0];
    }
}
