package ch22.blockingqueue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;

/**
 * Runs the same producer/consumer workload against all four bounded buffers,
 * checks that nothing is lost, duplicated or reordered, then shows the cost of
 * busy-waiting and how each version reacts to an interrupt.
 */
public class Main {
    static final int CAPACITY = 4;
    static final int PRODUCERS = 3;
    static final int CONSUMERS = 3;
    static final int ITEMS_PER_PRODUCER = 2_000;
    static final int POISON = -1;            // "no more items" marker

    public static void main(String[] args) throws Exception {
        List<IntFunction<BoundedBuffer<Integer>>> makers = List.of(
                MonitorBuffer::new, LockConditionBuffer::new, SemaphoreBuffer::new, SpinBuffer::new);

        System.out.println("== 1. correctness: " + PRODUCERS + " producers x " + ITEMS_PER_PRODUCER
                + " items, " + CONSUMERS + " consumers, capacity " + CAPACITY + " == [ms: machine-dependent]");
        for (IntFunction<BoundedBuffer<Integer>> make : makers) {
            correctness(make.apply(CAPACITY));
        }

        System.out.println();
        System.out.println("== 2. what a waiting consumer costs (buffer empty for 300 ms) == [ms: machine-dependent]");
        for (IntFunction<BoundedBuffer<Integer>> make : makers) {
            idleCost(make.apply(CAPACITY));
        }

        System.out.println();
        System.out.println("== 3. interrupting a consumer blocked on an empty buffer ==");
        for (IntFunction<BoundedBuffer<Integer>> make : makers) {
            interruptBlockedTake(make.apply(CAPACITY));
        }

        System.out.println();
        LockConditionBuffer<Integer> timed = new LockConditionBuffer<>(CAPACITY);
        long t0 = System.nanoTime();
        Integer got = timed.poll(100, TimeUnit.MILLISECONDS);
        long waited = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        System.out.println("poll(100 ms) on empty buffer -> " + got + ", waited >= 100 ms: " + (waited >= 100));
    }

    static void correctness(BoundedBuffer<Integer> buffer) throws InterruptedException {
        List<Thread> producers = new ArrayList<>();
        for (int p = 0; p < PRODUCERS; p++) {
            final int base = p * 1_000_000;            // producer p sends base+0 .. base+1999
            producers.add(Thread.ofPlatform().name("producer-" + p).start(() -> {
                try {
                    for (int i = 0; i < ITEMS_PER_PRODUCER; i++) buffer.put(base + i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        List<List<Integer>> received = new ArrayList<>();
        List<Thread> consumers = new ArrayList<>();
        for (int c = 0; c < CONSUMERS; c++) {
            List<Integer> mine = new ArrayList<>();
            received.add(mine);
            consumers.add(Thread.ofPlatform().name("consumer-" + c).start(() -> {
                try {
                    while (true) {
                        int v = buffer.take();
                        if (v == POISON) return;
                        mine.add(v);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        long start = System.nanoTime();
        for (Thread t : producers) t.join();
        for (int c = 0; c < CONSUMERS; c++) buffer.put(POISON);   // one pill per consumer
        for (Thread t : consumers) t.join();                       // join = happens-before: lists are safe to read
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        Set<Integer> seen = new HashSet<>();
        int total = 0;
        boolean perProducerOrder = true;
        for (List<Integer> mine : received) {
            int[] last = new int[PRODUCERS];
            java.util.Arrays.fill(last, -1);
            for (int v : mine) {
                total++;
                seen.add(v);
                int p = v / 1_000_000, i = v % 1_000_000;
                if (i <= last[p]) perProducerOrder = false;   // FIFO: each consumer sees a producer's items in order
                last[p] = i;
            }
        }
        System.out.printf("%-34s received=%d unique=%d FIFO=%b left=%d (%d ms)%n",
                buffer.name(), total, seen.size(), perProducerOrder, buffer.size(), ms);
    }

    static void idleCost(BoundedBuffer<Integer> buffer) throws InterruptedException {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        CountDownLatch started = new CountDownLatch(1);
        AtomicReference<Integer> got = new AtomicReference<>();
        Thread consumer = Thread.ofPlatform().name("idle-consumer").start(() -> {
            started.countDown();
            try {
                got.set(buffer.take());          // blocks (or spins) for ~300 ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        started.await();
        Thread.sleep(20);                        // let it reach take()
        long cpuBefore = mx.getThreadCpuTime(consumer.threadId());
        Thread.sleep(300);                       // buffer stays empty
        long cpuAfter = mx.getThreadCpuTime(consumer.threadId());
        buffer.put(42);
        consumer.join();
        long cpuMs = TimeUnit.NANOSECONDS.toMillis(cpuAfter - cpuBefore);
        System.out.printf("%-34s got %d, CPU used while waiting = %3d ms, burned a core? %b%n",
                buffer.name(), got.get(), cpuMs, cpuMs > 100);
    }

    static void interruptBlockedTake(BoundedBuffer<Integer> buffer) throws InterruptedException {
        AtomicReference<String> outcome = new AtomicReference<>("still blocked");
        Thread consumer = Thread.ofPlatform().name("blocked-consumer").start(() -> {
            try {
                buffer.take();
                outcome.set("returned an item?!");
            } catch (InterruptedException e) {
                outcome.set("InterruptedException, interrupt flag now " + Thread.currentThread().isInterrupted());
            }
        });
        Thread.sleep(50);
        consumer.interrupt();
        consumer.join(1_000);
        System.out.printf("%-34s %s%n", buffer.name(), outcome.get());
    }
}
