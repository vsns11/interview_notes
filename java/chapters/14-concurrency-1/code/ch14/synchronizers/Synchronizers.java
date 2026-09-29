package ch14.synchronizers;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** CountDownLatch, CyclicBarrier, Semaphore, Phaser. */
public class Synchronizers {
    public static void main(String[] args) throws Exception {
        // 1) CountDownLatch: a one-shot gate. Start gate (1) + finish line (N).
        int n = 4;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(n);
        AtomicInteger work = new AtomicInteger();
        for (int i = 0; i < n; i++) {
            new Thread(() -> {
                try { startGate.await(); work.addAndGet(10); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { finished.countDown(); }
            }).start();
        }
        System.out.println("latch: work before opening the gate = " + work.get());
        startGate.countDown();                                   // release all workers at once
        finished.await();                                        // wait for all N
        System.out.println("latch: work after all finished = " + work.get() + ", count = " + finished.getCount());

        // 2) CyclicBarrier: N threads meet at a point, repeatedly; an optional action runs once per trip
        AtomicInteger phase = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(3, () -> System.out.println("barrier: all 3 arrived, phase " + phase.incrementAndGet() + " complete"));
        Thread[] ts = new Thread[3];
        for (int i = 0; i < 3; i++) {
            ts[i] = new Thread(() -> {
                try { for (int round = 0; round < 2; round++) barrier.await(); }   // reused for 2 rounds
                catch (Exception e) { Thread.currentThread().interrupt(); }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();

        // 3) Semaphore: at most 3 permits -> at most 3 tasks in the critical region
        Semaphore permits = new Semaphore(3);
        AtomicInteger inside = new AtomicInteger(), maxInside = new AtomicInteger();
        try (ExecutorService pool = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 20; i++) {
                pool.submit(() -> {
                    permits.acquire();
                    try {
                        maxInside.accumulateAndGet(inside.incrementAndGet(), Math::max);
                        Thread.sleep(20);                            // e.g. a call to a rate-limited API
                        inside.decrementAndGet();
                    } finally {
                        permits.release();
                    }
                    return null;
                });
            }
        }
        System.out.println("semaphore: 20 tasks on 10 threads, max ever inside = " + maxInside.get() + " (limit 3)");

        // 4) Phaser: a reusable barrier whose party count can change
        Phaser phaser = new Phaser(1);                              // register main
        for (int i = 0; i < 2; i++) {
            phaser.register();
            new Thread(() -> { phaser.arriveAndAwaitAdvance(); phaser.arriveAndDeregister(); }).start();
        }
        phaser.arriveAndAwaitAdvance();
        System.out.println("phaser: main + 2 workers passed phase 0, current phase = " + phaser.getPhase());
        phaser.arriveAndDeregister();
    }
}
