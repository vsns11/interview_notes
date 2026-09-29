package ch14.executors;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;

/** ExecutorService: submit tasks, get Futures, invokeAll/invokeAny, shut down properly, schedule. */
public class ExecutorBasics {
    public static void main(String[] args) throws Exception {
        // try-with-resources: ExecutorService is AutoCloseable since Java 19; close() = shutdown + wait
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            Set<String> threadNames = new ConcurrentSkipListSet<>();

            // execute(Runnable): fire and forget
            CountDownLatch done = new CountDownLatch(6);
            for (int i = 0; i < 6; i++) pool.execute(() -> { threadNames.add(Thread.currentThread().getName()); done.countDown(); });
            done.await();
            System.out.println("6 tasks ran on at most 3 threads: " + (threadNames.size() <= 3));

            // submit(Callable): get a Future for the result
            Future<Integer> f = pool.submit(() -> { Thread.sleep(100); return 6 * 7; });
            System.out.println("isDone right after submit? " + f.isDone());
            System.out.println("get() blocks, then returns " + f.get());
            System.out.println("state = " + f.state() + ", resultNow = " + f.resultNow());   // Java 19

            // Exceptions are captured and rethrown from get() wrapped in ExecutionException
            Future<Integer> bad = pool.submit(() -> Integer.parseInt("forty-two"));
            try {
                bad.get();
            } catch (ExecutionException e) {
                System.out.println("get() -> ExecutionException caused by " + e.getCause().getClass().getSimpleName());
            }

            // get with timeout, then cancel
            Future<String> slow = pool.submit(() -> { Thread.sleep(10_000); return "late"; });
            try {
                slow.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("get(100ms) -> TimeoutException; cancel(true) = " + slow.cancel(true)
                        + ", isCancelled = " + slow.isCancelled());
            }

            // invokeAll: run a batch, wait for all; results in submission order
            List<Callable<String>> jobs = List.of(() -> "a", () -> { Thread.sleep(50); return "b"; }, () -> "c");
            StringBuilder sb = new StringBuilder();
            for (Future<String> r : pool.invokeAll(jobs)) sb.append(r.get());
            System.out.println("invokeAll results in order: " + sb);

            // invokeAny: first successful result wins, the rest are cancelled
            String fastest = pool.invokeAny(List.of(
                    () -> { Thread.sleep(300); return "slow mirror"; },
                    () -> { Thread.sleep(10); return "fast mirror"; }));
            System.out.println("invokeAny -> " + fastest);
        }
        System.out.println("pool closed (all tasks finished)");

        // Classic shutdown sequence (pre-Java-19 style, still what close() does internally)
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.submit(() -> System.out.println("single-thread executor task"));
        ex.shutdown();                                        // no new tasks; queued ones still run
        System.out.println("awaitTermination -> " + ex.awaitTermination(5, TimeUnit.SECONDS) + ", isTerminated = " + ex.isTerminated());
        try {
            ex.submit(() -> {});
        } catch (RejectedExecutionException e) {
            System.out.println("submit after shutdown -> RejectedExecutionException");
        }

        // ScheduledExecutorService: delays and periodic tasks
        try (ScheduledExecutorService sched = Executors.newScheduledThreadPool(1)) {
            CountDownLatch ticks = new CountDownLatch(3);
            long start = System.nanoTime();
            ScheduledFuture<?> ticker = sched.scheduleAtFixedRate(() -> {
                System.out.printf("tick %d at ~%d ms   [timing approximate]%n", 3 - ticks.getCount() + 1,
                        Math.round((System.nanoTime() - start) / 1e6 / 50) * 50);
                ticks.countDown();
            }, 50, 100, TimeUnit.MILLISECONDS);
            ticks.await();
            ticker.cancel(false);
            ScheduledFuture<String> once = sched.schedule(() -> "delayed hello", 50, TimeUnit.MILLISECONDS);
            System.out.println(once.get());
        }
    }
}
