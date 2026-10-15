package ch15.exceptions;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** Triggers each common concurrency exception once and prints what was thrown. */
public class ExceptionsTour {
    interface Action { void run() throws Exception; }

    static void show(String label, Action a) {
        try { a.run(); System.out.println(label + " -> no exception"); }
        catch (Exception e) {
            String cause = e.getCause() == null ? "" : " (cause: " + e.getCause().getClass().getSimpleName() + ")";
            System.out.printf("%-42s -> %s%s%n", label, e.getClass().getSimpleName(), cause);
        }
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        show("sleep() on an interrupted thread", () -> {
            Thread.currentThread().interrupt();
            Thread.sleep(1_000);
        });
        show("Future.get(), task threw", () -> pool.submit(() -> { throw new IllegalArgumentException("bad"); }).get());
        show("Future.get(50 ms), task too slow", () -> pool.submit(() -> { Thread.sleep(300); return 1; })
                .get(50, TimeUnit.MILLISECONDS));
        show("Future.get(), task was cancelled", () -> {
            Future<?> f = pool.submit(() -> { Thread.sleep(5_000); return 1; });
            f.cancel(true);
            f.get();
        });
        show("CompletableFuture.join(), stage threw", () ->
                CompletableFuture.supplyAsync(() -> { throw new IllegalArgumentException("bad"); }).join());

        show("CyclicBarrier, another party timed out", () -> {
            CyclicBarrier barrier = new CyclicBarrier(3);
            Thread impatient = new Thread(() -> {
                try { barrier.await(50, TimeUnit.MILLISECONDS); } catch (Exception ignored) { }  // TimeoutException here
            });
            impatient.start();
            barrier.await();                          // main is woken by the break
        });
        show("Object.wait() without the monitor", () -> new Object().wait());
        show("unlock() by a non-owner", () -> new ReentrantLock().unlock());
        show("Condition.await() without the lock", () -> {
            Condition c = new ReentrantLock().newCondition();
            c.await();
        });
        show("Thread.start() twice", () -> { Thread t = new Thread(() -> { }); t.start(); t.start(); });

        pool.shutdown();
        show("submit() after shutdown()", () -> pool.submit(() -> 1));
        ThreadPoolExecutor tiny = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
        show("submit() to a saturated pool (AbortPolicy)", () -> {
            tiny.submit(() -> { Thread.sleep(200); return 1; });
            tiny.submit(() -> 2);                     // no thread free, no queue space
        });
        tiny.shutdown();
        System.out.println("interrupt flag after InterruptedException was thrown: " + Thread.currentThread().isInterrupted());
    }
}
