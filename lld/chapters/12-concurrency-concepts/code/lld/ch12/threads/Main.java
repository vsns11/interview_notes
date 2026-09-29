package lld.ch12.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/** Five ways to run code on another thread, plus a tour of every Thread.State. */
public class Main {

    // Way 1: subclass Thread (works, but couples "the task" with "the worker").
    static class GreeterThread extends Thread {
        GreeterThread() { super("greeter-thread"); }
        @Override public void run() {
            System.out.println("1) extends Thread   -> hello from " + getName());
        }
    }

    public static void main(String[] args) throws Exception {
        creatingThreads();
        virtualThreads();
        threadStates();
    }

    static void creatingThreads() throws Exception {
        System.out.println("=== Creating threads ===");
        Thread t1 = new GreeterThread();
        t1.start();          // start() creates a new call stack; run() would run on *this* thread
        t1.join();           // wait for it to finish so output order is predictable

        // Way 2: Runnable = a task with no result. Preferred over subclassing Thread.
        Runnable task = () -> System.out.println("2) Runnable         -> hello from " + Thread.currentThread().getName());
        Thread t2 = new Thread(task, "runnable-thread");
        t2.start();
        t2.join();

        // Way 3 + 4: Callable (returns a value / throws) submitted to an ExecutorService (a thread pool).
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> sumTask = () -> IntStream.rangeClosed(1, 100).sum();
            Future<Integer> future = pool.submit(sumTask);   // returns immediately
            System.out.println("3) Callable+Future  -> sum 1..100 = " + future.get()); // get() blocks until done

            List<Callable<String>> jobs = List.of(() -> "A", () -> "B", () -> "C");
            List<String> results = new ArrayList<>();
            for (Future<String> f : pool.invokeAll(jobs)) results.add(f.get());   // invokeAll keeps input order
            System.out.println("4) ExecutorService  -> invokeAll results " + results);
        } finally {
            pool.shutdown();                                   // stop accepting work
            pool.awaitTermination(5, TimeUnit.SECONDS);       // wait for running tasks
        }
    }

    static void virtualThreads() throws Exception {
        System.out.println("=== Virtual threads (Java 21) ===");
        Thread vt = Thread.ofVirtual().name("virtual-1").start(
                () -> System.out.println("5) virtual thread   -> isVirtual=" + Thread.currentThread().isVirtual()));
        vt.join();

        // 10,000 blocking tasks: cheap with virtual threads (each sleep parks the virtual thread,
        // freeing its carrier OS thread for other work).
        long start = System.nanoTime();
        int completed;
        try (ExecutorService vexec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < 10_000; i++) {
                fs.add(vexec.submit(() -> { Thread.sleep(50); return 1; }));
            }
            int sum = 0;
            for (Future<Integer> f : fs) sum += f.get();
            completed = sum;
        } // close() waits for all tasks
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("10,000 tasks that each sleep 50 ms completed: " + completed
                + " (well under 10,000 x 50 ms = 500 s: " + (ms < 60_000) + ")");
    }

    static void threadStates() throws Exception {
        System.out.println("=== Thread.State tour ===");
        Object monitor = new Object();

        Thread sleeper = new Thread(() -> {
            try { Thread.sleep(10_000); } catch (InterruptedException e) { /* woken early: fine */ }
        }, "sleeper");
        System.out.println("after new Thread()            : " + sleeper.getState());      // NEW
        sleeper.start();
        awaitState(sleeper, Thread.State.TIMED_WAITING);
        System.out.println("inside Thread.sleep(10s)      : " + sleeper.getState());      // TIMED_WAITING
        sleeper.interrupt();
        sleeper.join();
        System.out.println("after run() returns           : " + sleeper.getState());      // TERMINATED

        CountDownLatch gate = new CountDownLatch(1);
        Thread waiter = new Thread(() -> {
            try { gate.await(); } catch (InterruptedException ignored) { }
        }, "waiter");
        waiter.start();
        awaitState(waiter, Thread.State.WAITING);
        System.out.println("inside latch.await()          : " + waiter.getState());       // WAITING
        gate.countDown();
        waiter.join();

        Thread blocked;
        synchronized (monitor) {             // main holds the monitor...
            blocked = new Thread(() -> { synchronized (monitor) { /* got it */ } }, "blocked");
            blocked.start();
            awaitState(blocked, Thread.State.BLOCKED);
            System.out.println("waiting to enter synchronized : " + blocked.getState()); // BLOCKED
        }                                    // ...released here, so 'blocked' can proceed
        blocked.join();

        java.util.concurrent.atomic.AtomicBoolean stop = new java.util.concurrent.atomic.AtomicBoolean();
        Thread spinner = new Thread(() -> { while (!stop.get()) Thread.onSpinWait(); }, "spinner");
        spinner.start();
        System.out.println("busy running on a CPU         : " + spinner.getState());      // RUNNABLE
        stop.set(true);
        spinner.join();
    }

    /** Poll until the thread reaches the wanted state (max ~5 s). */
    static void awaitState(Thread t, Thread.State wanted) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (t.getState() != wanted && System.nanoTime() < deadline) Thread.sleep(1);
    }
}
