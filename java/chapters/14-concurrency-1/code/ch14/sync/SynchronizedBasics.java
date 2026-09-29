package ch14.sync;

import java.util.concurrent.CountDownLatch;

/** What a synchronized method/block locks, reentrancy, and that different locks do NOT exclude each other. */
public class SynchronizedBasics {

    static class Counter {
        private int value;
        public synchronized void increment() { value++; }           // locks 'this'
        public void incrementBlock() { synchronized (this) { value++; } }   // exactly equivalent
        public synchronized int get() { return value; }             // reads need the lock too (visibility)
        public synchronized void addTwice() { increment(); increment(); }   // reentrant: same thread re-enters
        public static synchronized String describe() { return "locks Counter.class"; }   // class-level lock
    }

    public static void main(String[] args) throws InterruptedException {
        Counter c = new Counter();
        Thread[] ts = new Thread[4];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new Thread(() -> { for (int k = 0; k < 250_000; k++) { c.increment(); c.incrementBlock(); } });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.println("4 threads x 500,000 synchronized increments = " + c.get());

        c.addTwice();
        System.out.println("reentrant call worked, value = " + c.get());
        System.out.println("static synchronized " + Counter.describe());

        // holdsLock shows which monitor the current thread owns
        synchronized (c) {
            System.out.println("inside synchronized(c): holdsLock(c)=" + Thread.holdsLock(c)
                    + " holdsLock(Counter.class)=" + Thread.holdsLock(Counter.class));
        }

        // Two threads holding DIFFERENT locks run the critical sections at the same time
        Object lockA = new Object(), lockB = new Object();
        CountDownLatch bothInside = new CountDownLatch(2);
        Runnable r1 = () -> { synchronized (lockA) { bothInside.countDown(); awaitQuietly(bothInside); } };
        Runnable r2 = () -> { synchronized (lockB) { bothInside.countDown(); awaitQuietly(bothInside); } };
        Thread t1 = new Thread(r1), t2 = new Thread(r2);
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("different locks -> both threads were inside at once (latch reached 0: "
                + (bothInside.getCount() == 0) + ")");
    }

    static void awaitQuietly(CountDownLatch l) {
        try { l.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
