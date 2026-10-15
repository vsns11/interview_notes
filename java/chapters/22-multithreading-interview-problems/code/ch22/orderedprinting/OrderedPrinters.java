package ch22.orderedprinting;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * "Print in order": three threads call first(), second() and third() in any order;
 * the printouts must always come out first, second, third.
 * Four correct ways to do it, plus one broken way for comparison.
 */
public final class OrderedPrinters {
    private OrderedPrinters() {}

    /** The contract every solution implements. Each method is called once, by its own thread. */
    public interface Printer {
        void first(Runnable printFirst) throws InterruptedException;
        void second(Runnable printSecond) throws InterruptedException;
        void third(Runnable printThird) throws InterruptedException;
    }

    /** BROKEN: no coordination at all - prints in whatever order the threads arrive. */
    public static final class Unsynchronized implements Printer {
        public void first(Runnable p)  { p.run(); }
        public void second(Runnable p) { p.run(); }
        public void third(Runnable p)  { p.run(); }
    }

    /** Way 1: two one-shot gates. A latch that reached zero stays open forever. */
    public static final class LatchPrinter implements Printer {
        private final CountDownLatch firstDone = new CountDownLatch(1);
        private final CountDownLatch secondDone = new CountDownLatch(1);

        public void first(Runnable p) { p.run(); firstDone.countDown(); }
        public void second(Runnable p) throws InterruptedException {
            firstDone.await();                 // returns immediately if first() already finished
            p.run();
            secondDone.countDown();
        }
        public void third(Runnable p) throws InterruptedException {
            secondDone.await();
            p.run();
        }
    }

    /** Way 2: two semaphores that start with ZERO permits; release() is the "you may go" signal. */
    public static final class SemaphorePrinter implements Printer {
        private final Semaphore canRunSecond = new Semaphore(0);
        private final Semaphore canRunThird = new Semaphore(0);

        public void first(Runnable p) { p.run(); canRunSecond.release(); }
        public void second(Runnable p) throws InterruptedException {
            canRunSecond.acquire();            // the permit is remembered even if released earlier
            p.run();
            canRunThird.release();
        }
        public void third(Runnable p) throws InterruptedException {
            canRunThird.acquire();
            p.run();
        }
    }

    /** Way 3: a monitor (synchronized + wait/notifyAll) guarding one int "stage" field. */
    public static final class MonitorPrinter implements Printer {
        private int stage = 1;                 // whose turn it is: 1, 2 or 3; guarded by 'this'

        private synchronized void runAt(int myStage, Runnable p) throws InterruptedException {
            while (stage != myStage) {         // while, not if: spurious wakeups and wrong-turn wakeups
                wait();
            }
            p.run();
            stage++;
            notifyAll();                       // wake everyone; the right one will see its stage
        }
        public void first(Runnable p) throws InterruptedException  { runAt(1, p); }
        public void second(Runnable p) throws InterruptedException { runAt(2, p); }
        public void third(Runnable p) throws InterruptedException  { runAt(3, p); }
    }

    /** Way 4: a volatile flag and busy-waiting. Correct, but burns a CPU core while waiting. */
    public static final class SpinPrinter implements Printer {
        private volatile int stage = 1;        // volatile: writes are visible to the spinning threads

        private void runAt(int myStage, Runnable p) {
            while (stage != myStage) {
                Thread.onSpinWait();           // hint to the CPU that this is a spin loop
            }
            p.run();
            stage = myStage + 1;               // only one thread ever writes at a time, so no CAS needed
        }
        public void first(Runnable p)  { runAt(1, p); }
        public void second(Runnable p) { runAt(2, p); }
        public void third(Runnable p)  { runAt(3, p); }
    }
}
