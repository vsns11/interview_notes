package ch14.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/** ThreadPoolExecutor's knobs: core/max threads, a bounded queue and a rejection policy. */
public class PoolConfig {
    public static void main(String[] args) throws Exception {
        System.out.println("available processors: " + Runtime.getRuntime().availableProcessors() + "   [machine-dependent]");

        CountDownLatch block = new CountDownLatch(1);
        Runnable stuck = () -> { try { block.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } };

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2, 3,                                   // core 2 threads, grow to max 3 ...
                30, TimeUnit.SECONDS,                   // ... extra threads die after 30 s idle
                new ArrayBlockingQueue<>(2),            // at most 2 waiting tasks
                new ThreadPoolExecutor.AbortPolicy());  // when full: throw

        List<String> log = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            try {
                pool.execute(stuck);
                log.add("task " + i + " accepted  (threads=" + pool.getPoolSize() + ", queued=" + pool.getQueue().size() + ")");
            } catch (RejectedExecutionException e) {
                log.add("task " + i + " REJECTED  (threads=" + pool.getPoolSize() + ", queued=" + pool.getQueue().size() + ")");
            }
        }
        log.forEach(System.out::println);
        block.countDown();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("completed tasks = " + pool.getCompletedTaskCount());

        // CallerRunsPolicy: back-pressure - the submitting thread runs the task itself
        ThreadPoolExecutor cr = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());
        CountDownLatch block2 = new CountDownLatch(1);
        cr.execute(() -> { try { block2.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        cr.execute(() -> {});                                    // queued
        cr.execute(() -> System.out.println("third task ran in: " + Thread.currentThread().getName() + " (the caller)"));
        block2.countDown();
        cr.shutdown();
        cr.awaitTermination(5, TimeUnit.SECONDS);
    }
}
