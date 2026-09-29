package ch14.threads;

import java.util.concurrent.CountDownLatch;

/** Interruption is a polite request to stop - the thread decides how to react. */
public class Interruption {
    public static void main(String[] args) throws InterruptedException {
        // 1) Interrupting a blocked thread: sleep() throws InterruptedException
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(60_000);
                System.out.println("never printed");
            } catch (InterruptedException e) {
                System.out.println("sleeper: woke up early, flag now = " + Thread.currentThread().isInterrupted());
                Thread.currentThread().interrupt();              // restore the flag for callers
                System.out.println("sleeper: flag restored = " + Thread.currentThread().isInterrupted());
            }
        });
        sleeper.start();
        Thread.sleep(100);
        sleeper.interrupt();
        sleeper.join();

        // 2) Interrupting a busy thread: nothing happens unless it checks the flag
        CountDownLatch started = new CountDownLatch(1);
        Thread worker = new Thread(() -> {
            long loops = 0;
            started.countDown();
            while (!Thread.currentThread().isInterrupted()) loops++;   // cooperative check
            System.out.println("worker: saw the interrupt and stopped cleanly (loops > 0: " + (loops > 0) + ")");
        });
        worker.start();
        started.await();
        worker.interrupt();
        worker.join();

        // 3) Thread.interrupted() READS AND CLEARS the current thread's flag
        Thread.currentThread().interrupt();
        System.out.println("interrupted() first call  = " + Thread.interrupted());
        System.out.println("interrupted() second call = " + Thread.interrupted());
    }
}
