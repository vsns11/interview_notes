package lld.ch12.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Signaling, thread pool, producer-consumer, immutability, thread confinement. */
public class Main {

    // ---------- 1. Signaling ----------
    static void signaling() throws InterruptedException {
        System.out.println("=== Signaling: B must run after A ===");
        Semaphore aDone = new Semaphore(0);                  // 0 permits = "not signalled yet"
        List<String> log = java.util.Collections.synchronizedList(new ArrayList<>());
        Thread b = new Thread(() -> {
            try { aDone.acquire(); log.add("B: send email (after payment)"); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread a = new Thread(() -> { log.add("A: charge payment"); aDone.release(); });
        b.start(); a.start();                                // B starts first but must wait
        a.join(); b.join();
        log.forEach(System.out::println);
    }

    // ---------- 2. Thread pool (hand-made, to see the moving parts) ----------
    static class SimpleThreadPool {
        private static final Runnable POISON = () -> { };
        private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
        private final List<Thread> workers = new ArrayList<>();
        private boolean shutdown;                        // guarded by 'this'

        SimpleThreadPool(int size) {
            for (int i = 0; i < size; i++) {
                Thread w = new Thread(() -> {
                    try {
                        while (true) {
                            Runnable r = tasks.take();          // idle workers block here
                            if (r == POISON) return;
                            try { r.run(); } catch (RuntimeException e) { System.err.println("task failed: " + e); }
                        }
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }, "pool-worker-" + i);
                workers.add(w);
                w.start();
            }
        }
        // submit and the pill-adding part of shutdown share one lock, so "check the flag, then
        // enqueue" is atomic: no task can slip into the queue AFTER the poison pills and be lost.
        synchronized void submit(Runnable r) {
            if (shutdown) throw new RejectedExecutionException("pool is shut down");
            tasks.add(r);                                // unbounded queue: add() never blocks
        }
        void shutdownAndWait() throws InterruptedException {
            synchronized (this) {
                if (shutdown) return;
                shutdown = true;
                for (int i = 0; i < workers.size(); i++) tasks.add(POISON);   // one pill per worker
            }
            for (Thread w : workers) w.join();           // wait OUTSIDE the lock
        }
    }

    static void threadPool() throws InterruptedException {
        System.out.println("=== Thread pool ===");
        SimpleThreadPool pool = new SimpleThreadPool(3);
        AtomicInteger done = new AtomicInteger();
        for (int i = 0; i < 20; i++) pool.submit(done::incrementAndGet);
        pool.shutdownAndWait();
        System.out.println("hand-made pool ran " + done.get() + " tasks on 3 threads");

        // Production version: bounded queue + explicit rejection policy.
        ThreadPoolExecutor exec = new ThreadPoolExecutor(
                2, 4,                                   // core and max threads
                30, TimeUnit.SECONDS,                   // idle time before extra threads die
                new ArrayBlockingQueue<>(100),          // bounded: never an OOM from a huge backlog
                new ThreadPoolExecutor.CallerRunsPolicy()); // when full, the submitter runs it (back-pressure)
        CompletableFuture<Integer> price = CompletableFuture.supplyAsync(() -> 100, exec);
        CompletableFuture<Integer> tax = CompletableFuture.supplyAsync(() -> 18, exec);
        System.out.println("ThreadPoolExecutor + CompletableFuture: total = " + price.thenCombine(tax, Integer::sum).join());
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }

    // ---------- 3. Producer-consumer ----------
    static void producerConsumer() throws InterruptedException {
        System.out.println("=== Producer-consumer ===");
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);   // producers block when full
        final int POISON = -1, CONSUMERS = 2;
        AtomicLong total = new AtomicLong();
        List<Thread> consumers = new ArrayList<>();
        for (int c = 0; c < CONSUMERS; c++) {
            Thread t = new Thread(() -> {
                try {
                    for (int x = queue.take(); x != POISON; x = queue.take()) total.addAndGet(x);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            consumers.add(t); t.start();
        }
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 1_000; i++) queue.put(i);
                for (int c = 0; c < CONSUMERS; c++) queue.put(POISON);   // tell each consumer to stop
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start();
        producer.join();
        for (Thread t : consumers) t.join();
        System.out.println("consumed sum of 1..1000 = " + total.get() + " (expected 500500)");
    }

    // ---------- 4. Immutability ----------
    /** Immutable value object: final fields, no setters, defensive copy. Safe to share freely. */
    record Order(String id, List<String> items) {
        Order { items = List.copyOf(items); }                  // caller's list can't change ours
        Order withItem(String item) {                           // "modify" = return a new object
            List<String> copy = new ArrayList<>(items); copy.add(item);
            return new Order(id, copy);
        }
    }

    // ---------- 5. Thread confinement ----------
    private static final ThreadLocal<StringBuilder> BUFFER = ThreadLocal.withInitial(StringBuilder::new);

    static String render(String name) {
        StringBuilder sb = BUFFER.get();       // each thread has its own builder: no sharing, no locks
        sb.setLength(0);
        return sb.append("Hello, ").append(name).toString();
    }

    public static void main(String[] args) throws Exception {
        signaling();
        threadPool();
        producerConsumer();

        System.out.println("=== Immutability ===");
        List<String> mutable = new ArrayList<>(List.of("pen"));
        Order o1 = new Order("o1", mutable);
        mutable.add("hacked");
        Order o2 = o1.withItem("book");
        System.out.println("o1 = " + o1.items() + ", o2 = " + o2.items());

        System.out.println("=== Thread confinement (ThreadLocal) ===");
        Thread t1 = new Thread(() -> System.out.println(render("T1")));
        t1.start(); t1.join();
        Thread t2 = new Thread(() -> System.out.println(render("T2")));
        t2.start(); t2.join();
    }
}
