package lld.ch12.coordination;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Semaphore, wait/notify, Condition, CountDownLatch, CyclicBarrier. */
public class Main {

    // ---------- Condition variable with the built-in monitor (wait / notifyAll) ----------
    /** A one-slot mailbox: put waits while full, take waits while empty. */
    static class Mailbox<T> {
        private T item;                                   // guarded by 'this'
        public synchronized void put(T x) throws InterruptedException {
            while (item != null) wait();                  // ALWAYS wait in a loop (spurious wakeups)
            item = x;
            notifyAll();                                  // wake the taker(s)
        }
        public synchronized T take() throws InterruptedException {
            while (item == null) wait();
            T x = item;
            item = null;
            notifyAll();                                  // wake the putter(s)
            return x;
        }
    }

    // ---------- Condition variable with ReentrantLock + Condition ----------
    /** Two threads take turns: PING, PONG, PING, PONG ... */
    static class TurnTable {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition turnChanged = lock.newCondition();
        private boolean pingsTurn = true;

        void play(boolean iAmPing, String word, int rounds, List<String> out) throws InterruptedException {
            for (int i = 0; i < rounds; i++) {
                lock.lock();
                try {
                    while (pingsTurn != iAmPing) turnChanged.await();   // releases lock while waiting
                    out.add(word);
                    pingsTurn = !pingsTurn;
                    turnChanged.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        semaphoreDemo();
        waitNotifyDemo();
        conditionDemo();
        latchDemo();
        barrierDemo();
    }

    static void semaphoreDemo() throws InterruptedException {
        System.out.println("=== Semaphore: at most 3 concurrent DB connections ===");
        Semaphore connections = new Semaphore(3);
        AtomicInteger inUse = new AtomicInteger(), maxSeen = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> {
                try {
                    connections.acquire();                         // blocks when 0 permits left
                    try {
                        int now = inUse.incrementAndGet();
                        maxSeen.accumulateAndGet(now, Math::max);
                        Thread.sleep(30);                          // "run a query"
                        inUse.decrementAndGet();
                    } finally {
                        connections.release();                     // give the permit back
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("10 queries done, never more than 3 at once: " + (maxSeen.get() <= 3));
    }

    static void waitNotifyDemo() throws InterruptedException {
        System.out.println("=== wait/notifyAll: one-slot mailbox ===");
        Mailbox<String> box = new Mailbox<>();
        List<String> received = new ArrayList<>();
        Thread consumer = new Thread(() -> {
            try { for (int i = 0; i < 3; i++) received.add(box.take()); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        consumer.start();
        for (String m : List.of("m1", "m2", "m3")) box.put(m);
        consumer.join();
        System.out.println("received in order: " + received);
    }

    static void conditionDemo() throws InterruptedException {
        System.out.println("=== Lock + Condition: strict turn-taking ===");
        TurnTable table = new TurnTable();
        List<String> out = java.util.Collections.synchronizedList(new ArrayList<>());
        Thread ping = new Thread(() -> { try { table.play(true, "PING", 3, out); } catch (InterruptedException ignored) { } });
        Thread pong = new Thread(() -> { try { table.play(false, "PONG", 3, out); } catch (InterruptedException ignored) { } });
        pong.start(); ping.start();              // start order does not matter
        ping.join(); pong.join();
        System.out.println(String.join(" ", out));
    }

    static void latchDemo() throws InterruptedException {
        System.out.println("=== CountDownLatch: wait for 3 services to start ===");
        CountDownLatch ready = new CountDownLatch(3);
        for (String svc : List.of("db", "cache", "search")) {
            new Thread(() -> { /* ...warm up... */ ready.countDown(); }, svc).start();
        }
        boolean allUp = ready.await(2, TimeUnit.SECONDS);   // one-shot: cannot be reset
        System.out.println("all services ready: " + allUp + " (count now " + ready.getCount() + ")");
    }

    static void barrierDemo() throws Exception {
        System.out.println("=== CyclicBarrier: 3 workers, 2 phases ===");
        AtomicInteger phase = new AtomicInteger();
        // The barrier action runs once per trip, by the last thread to arrive.
        CyclicBarrier barrier = new CyclicBarrier(3,
                () -> System.out.println("phase " + phase.incrementAndGet() + " complete: all 3 workers arrived"));
        List<Thread> ws = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(() -> {
                try {
                    for (int p = 0; p < 2; p++) {
                        Thread.sleep((long) (Math.random() * 20)); // uneven work
                        barrier.await();                          // reusable: works for every phase
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws.add(t);
            t.start();
        }
        for (Thread t : ws) t.join();
    }
}
