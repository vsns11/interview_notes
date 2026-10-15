package ch22.deferredcallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    record Ran(String name, long dueMs, long ranMs) {}

    public static void main(String[] args) throws Exception {
        outOfOrderScheduling();
        earlierTaskWhileSleeping();
        throwingCallbackAndClose();
    }

    static long msSince(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    /** 1) Four threads schedule with delays 400, 100, 300, 200 ms; callbacks must run by deadline. */
    static void outOfOrderScheduling() throws Exception {
        List<Ran> log = new CopyOnWriteArrayList<>();
        CountDownLatch allRan = new CountDownLatch(4);
        long start = System.nanoTime();
        try (DeferredCallbackExecutor ex = new DeferredCallbackExecutor()) {
            String[] names = {"A", "B", "C", "D"};
            long[] delays = {400, 100, 300, 200};
            List<Thread> schedulers = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                final String name = names[i];
                final long delay = delays[i];
                schedulers.add(Thread.ofPlatform().start(() -> {
                    long due = msSince(start) + delay;
                    ex.schedule(() -> { log.add(new Ran(name, due, msSince(start))); allRan.countDown(); },
                            delay, TimeUnit.MILLISECONDS);
                }));
            }
            for (Thread t : schedulers) t.join();
            allRan.await();
        }
        StringBuilder order = new StringBuilder();
        boolean noneEarly = true;
        for (Ran r : log) {
            order.append(r.name()).append(' ');
            if (r.ranMs() < r.dueMs()) noneEarly = false;
        }
        System.out.println("1 scheduled A(400) B(100) C(300) D(200) from 4 threads -> ran " + order.toString().trim()
                + ", none ran before its deadline: " + noneEarly);
        for (Ran r : log) {
            System.out.println("   " + r.name() + " due ~" + r.dueMs() + " ms, ran at " + r.ranMs() + " ms [nondeterministic]");
        }
    }

    /** 2) The worker is asleep until X (500 ms); Y (deadline 150 ms) arrives and must not wait for X. */
    static void earlierTaskWhileSleeping() throws Exception {
        List<Ran> log = new CopyOnWriteArrayList<>();
        CountDownLatch bothRan = new CountDownLatch(2);
        long start = System.nanoTime();
        try (DeferredCallbackExecutor ex = new DeferredCallbackExecutor()) {
            ex.schedule(() -> { log.add(new Ran("X", 500, msSince(start))); bothRan.countDown(); },
                    500, TimeUnit.MILLISECONDS);
            Thread.sleep(50);                                       // worker is now in awaitNanos(~500 ms)
            ex.schedule(() -> { log.add(new Ran("Y", 150, msSince(start))); bothRan.countDown(); },
                    100, TimeUnit.MILLISECONDS);
            bothRan.await();
        }
        Ran y = log.get(0);
        System.out.println("2 X due at 500 ms, Y (due 150 ms) scheduled while worker sleeps -> first ran: " + y.name()
                + ", Y ran between 150 and 300 ms: " + (y.ranMs() >= 150 && y.ranMs() < 300)
                + ", then " + log.get(1).name());
    }

    /** 3) A throwing callback must not kill the worker; close() drains pending tasks, then refuses new ones. */
    static void throwingCallbackAndClose() throws Exception {
        List<String> log = new CopyOnWriteArrayList<>();
        DeferredCallbackExecutor ex = new DeferredCallbackExecutor();
        ex.schedule(() -> { throw new IllegalStateException("boom"); }, 10, TimeUnit.MILLISECONDS);
        ex.schedule(() -> log.add("after-boom"), 50, TimeUnit.MILLISECONDS);
        ex.schedule(() -> log.add("pending-at-close"), 150, TimeUnit.MILLISECONDS);
        Thread.sleep(100);
        ex.close();                                                 // waits for the 150 ms task, then the worker exits
        System.out.println("3 callbacks that ran after the throwing one: " + log);
        try {
            ex.schedule(() -> log.add("too-late"), 0, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            System.out.println("3 schedule() after close() -> IllegalStateException: " + e.getMessage());
        }
    }
}
