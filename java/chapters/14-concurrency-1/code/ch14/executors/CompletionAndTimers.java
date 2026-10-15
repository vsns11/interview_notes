package ch14.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** ExecutorCompletionService (results in completion order) and java.util.Timer vs ScheduledThreadPoolExecutor. */
public class CompletionAndTimers {

    static Callable<String> download(String name, long ms) {
        return () -> { Thread.sleep(ms); return name + "(" + ms + "ms)"; };
    }

    public static void main(String[] args) throws Exception {
        List<Callable<String>> jobs = List.of(download("A", 300), download("B", 100), download("C", 200));

        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            // invokeAll: results come back in SUBMISSION order, after the slowest finishes
            List<String> inOrder = new ArrayList<>();
            for (Future<String> f : pool.invokeAll(jobs)) inOrder.add(f.get());
            System.out.println("invokeAll         : " + inOrder);

            // CompletionService: each result as soon as it is ready
            CompletionService<String> cs = new ExecutorCompletionService<>(pool);
            for (Callable<String> j : jobs) cs.submit(j);
            List<String> asReady = new ArrayList<>();
            for (int i = 0; i < jobs.size(); i++) asReady.add(cs.take().get());   // take() = next finished Future
            System.out.println("CompletionService : " + asReady);
        }

        // ---- java.util.Timer: ONE thread; an exception in any task kills the whole Timer ----
        Timer timer = new Timer("legacy-timer", true);                // daemon so it can't keep the JVM alive
        CountDownLatch boom = new CountDownLatch(1);
        timer.schedule(new TimerTask() {
            @Override public void run() { boom.countDown(); throw new IllegalStateException("task failed"); }
        }, 10);
        boom.await();
        Thread.sleep(100);                                             // let the Timer thread die
        try {
            timer.schedule(new TimerTask() { @Override public void run() { } }, 10);
        } catch (IllegalStateException e) {
            System.out.println("Timer after a task threw -> IllegalStateException: " + e.getMessage());
        }

        // ---- ScheduledThreadPoolExecutor: a failing task only cancels ITSELF ----
        ScheduledThreadPoolExecutor sched = new ScheduledThreadPoolExecutor(1);
        AtomicInteger healthyRuns = new AtomicInteger();
        ScheduledFuture<?> bad = sched.scheduleAtFixedRate(() -> { throw new IllegalStateException("task failed"); },
                10, 50, TimeUnit.MILLISECONDS);
        sched.scheduleAtFixedRate(healthyRuns::incrementAndGet, 10, 50, TimeUnit.MILLISECONDS);
        try {
            bad.get(1, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            System.out.println("STPE failing periodic task -> ExecutionException caused by "
                    + e.getCause().getClass().getSimpleName() + "; isDone = " + bad.isDone());
        }
        Thread.sleep(300);
        System.out.println("STPE other periodic task kept running: " + (healthyRuns.get() >= 3));
        sched.shutdown();
        System.out.println("STPE terminated after shutdown: " + sched.awaitTermination(1, TimeUnit.SECONDS));
    }
}
