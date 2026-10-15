package ch14.sync;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** Mutex vs semaphore ownership, Mesa-style wakeups (if vs while), and missed signals. */
public class MonitorPitfalls {

    // ---------- 1) A mutex has an owner; a semaphore has only a count ----------
    static void mutexVsSemaphore() throws InterruptedException {
        ReentrantLock mutex = new ReentrantLock();
        Semaphore binary = new Semaphore(1);
        mutex.lock(); mutex.lock();                                   // same thread again: fine (reentrant)
        System.out.println("1 mutex locked twice by its owner, holdCount = " + mutex.getHoldCount());
        binary.acquire();
        System.out.println("1 semaphore: same thread tries a 2nd permit -> " + binary.tryAcquire()
                + " (acquire() here would block forever)");
        Thread other = new Thread(() -> {
            try { mutex.unlock(); } catch (IllegalMonitorStateException e) {
                System.out.println("1 other thread unlock(): IllegalMonitorStateException (not the owner)");
            }
            binary.release();                                         // allowed: no ownership
            System.out.println("1 other thread release(): ok, permits = " + binary.availablePermits());
        });
        other.start(); other.join();
        binary.release();                                             // nobody acquired it...
        System.out.println("1 extra release() -> permits = " + binary.availablePermits() + " (a 'binary' semaphore now admits 2)");
        mutex.unlock(); mutex.unlock();
    }

    // ---------- 2) Mesa semantics: a woken thread must re-check its condition ----------
    static class Box {
        private final ArrayDeque<Integer> items = new ArrayDeque<>();
        synchronized int takeWithIf() throws InterruptedException {
            if (items.isEmpty()) wait();                              // BUG: checked once only
            return items.removeFirst();                               // may be empty again!
        }
        synchronized int takeWithWhile() throws InterruptedException {
            while (items.isEmpty()) wait();                           // re-check after every wakeup
            return items.removeFirst();
        }
        synchronized void put(int x) { items.add(x); notifyAll(); }
    }

    static void ifVersusWhile(boolean useWhile) throws InterruptedException {
        Box box = new Box();
        AtomicInteger got = new AtomicInteger(), failed = new AtomicInteger();
        Thread[] consumers = new Thread[2];
        for (int i = 0; i < 2; i++) {
            consumers[i] = new Thread(() -> {
                try {
                    if (useWhile) box.takeWithWhile(); else box.takeWithIf();
                    got.incrementAndGet();
                } catch (NoSuchElementException e) { failed.incrementAndGet();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            consumers[i].setDaemon(true);                             // a stuck consumer can't keep the JVM alive
            consumers[i].start();
        }
        waitUntilBothWaiting(consumers);
        box.put(1);                                                   // ONE item, notifyAll wakes BOTH
        if (useWhile) {
            // wait until one consumer finished (got item 1) and the other is back in wait()
            long end = System.nanoTime() + 5_000_000_000L;            // guard: never spin forever
            while (!(oneIs(consumers, Thread.State.TERMINATED) && oneIs(consumers, Thread.State.WAITING))) {
                if (System.nanoTime() - end > 0) throw new IllegalStateException("consumers did not settle");
                Thread.onSpinWait();
            }
            System.out.println("2 while: after 1 item, one consumer is back in wait() instead of failing");
            box.put(2);
        }
        for (Thread t : consumers) {
            t.join(5_000);                                            // guard: a bug shows as a message, not a hang
            if (t.isAlive()) throw new IllegalStateException(t.getName() + " still blocked after 5 s");
        }
        System.out.println("2 " + (useWhile ? "while" : "if   ") + ": consumers that got an item = " + got
                + ", crashed with NoSuchElementException = " + failed);
    }

    static boolean oneIs(Thread[] ts, Thread.State s) { return ts[0].getState() == s || ts[1].getState() == s; }

    static void waitUntilBothWaiting(Thread[] ts) {
        long end = System.nanoTime() + 5_000_000_000L;               // guard: never spin forever
        for (Thread t : ts)
            while (t.getState() != Thread.State.WAITING) {
                if (System.nanoTime() - end > 0) throw new IllegalStateException(t.getName() + " never reached wait()");
                Thread.onSpinWait();
            }
    }

    // ---------- 3) Missed signal: notify() before anyone waits is lost ----------
    static final Object LOCK = new Object();
    static boolean ready = false;                                      // guarded by LOCK

    static void missedSignal() throws InterruptedException {
        Thread signaller = new Thread(() -> { synchronized (LOCK) { LOCK.notify(); } });
        signaller.start(); signaller.join();                           // signal happens FIRST
        long t0 = System.nanoTime();
        synchronized (LOCK) { LOCK.wait(500); }                        // naive: nothing remembers the signal
        System.out.println("3 naive wait: still waiting until the 500 ms timeout: "
                + (ms(t0) >= 500) + " -> the notify() was lost");

        Thread fixedSignaller = new Thread(() -> { synchronized (LOCK) { ready = true; LOCK.notifyAll(); } });
        fixedSignaller.start(); fixedSignaller.join();
        t0 = System.nanoTime();
        synchronized (LOCK) { while (!ready) LOCK.wait(500); }         // the state flag remembers it
        System.out.println("3 flag + while: returned at once: " + (ms(t0) < 100));

        Semaphore sem = new Semaphore(0);
        sem.release();                                                 // signal first ...
        t0 = System.nanoTime();
        boolean ok = sem.tryAcquire(500, TimeUnit.MILLISECONDS);       // ... wait later
        System.out.println("3 semaphore: permit was remembered: " + (ok && ms(t0) < 100));
    }

    static long ms(long t0) { return (System.nanoTime() - t0) / 1_000_000; }

    public static void main(String[] args) throws InterruptedException {
        mutexVsSemaphore();
        ifVersusWhile(false);
        ifVersusWhile(true);
        missedSignal();
    }
}
