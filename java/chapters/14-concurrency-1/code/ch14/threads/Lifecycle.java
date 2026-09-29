package ch14.threads;

import java.util.concurrent.CountDownLatch;

/** Drive one thread through every Thread.State and observe each one. */
public class Lifecycle {
    static final Object LOCK = new Object();
    static volatile boolean keepSpinning = true;

    public static void main(String[] args) throws Exception {
        CountDownLatch go = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            try {
                while (keepSpinning) Thread.onSpinWait();   // RUNNABLE (busy on a CPU)
                Thread.sleep(300);                         // TIMED_WAITING
                go.await();                                // WAITING (park until counted down)
                synchronized (LOCK) { }                    // BLOCKED while main holds LOCK
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "subject");

        System.out.println("after new:              " + t.getState());
        t.start();
        waitFor(t, Thread.State.RUNNABLE);
        System.out.println("while spinning:         " + t.getState());
        keepSpinning = false;
        waitFor(t, Thread.State.TIMED_WAITING);
        System.out.println("during sleep(300):      " + t.getState());
        waitFor(t, Thread.State.WAITING);
        System.out.println("during latch.await():   " + t.getState());
        synchronized (LOCK) {
            go.countDown();
            waitFor(t, Thread.State.BLOCKED);
            System.out.println("waiting for a monitor:  " + t.getState());
        }
        t.join();
        System.out.println("after run() returns:    " + t.getState());
    }

    /** Poll until the thread reaches the wanted state (avoids flaky sleeps). */
    static void waitFor(Thread t, Thread.State s) throws InterruptedException {
        while (t.getState() != s) Thread.sleep(1);
    }
}
