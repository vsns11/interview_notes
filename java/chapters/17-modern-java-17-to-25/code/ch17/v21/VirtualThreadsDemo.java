package ch17.v21;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** JEP 444 (Java 21): 10,000 blocking tasks, one virtual thread each. Details: Chapter 15. */
public class VirtualThreadsDemo {
    public static void main(String[] args) {
        AtomicInteger done = new AtomicInteger();
        long start = System.nanoTime();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {   // close() waits for all tasks
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(200));            // simulated blocking I/O
                    done.incrementAndGet();
                    return null;
                });
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("tasks finished: " + done.get());
        System.out.println("took < 2 s    : " + (ms < 2_000) + "   (10,000 x 200 ms sequentially would be 2,000 s)");
        System.out.println("is virtual    : " + Thread.ofVirtual().unstarted(() -> {}).isVirtual());
    }
}
