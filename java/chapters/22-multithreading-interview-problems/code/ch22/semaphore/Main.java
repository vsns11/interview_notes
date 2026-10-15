package ch22.semaphore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        limitConcurrency();
        signalBeforeWait();
        timedTryAcquire();
        multiPermitAcquire();
        boundedRelease();
        interruptWhileWaiting();
        fairOrder();
    }

    /** 1) The classic use: at most 3 of 10 workers inside the guarded region at once. */
    static void limitConcurrency() throws InterruptedException {
        CountingSemaphore sem = new CountingSemaphore(3);
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger maxInside = new AtomicInteger();
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            workers.add(Thread.ofPlatform().name("worker-" + i).start(() -> {
                try {
                    sem.acquire();
                    try {
                        int now = inside.incrementAndGet();
                        maxInside.accumulateAndGet(now, Math::max);
                        Thread.sleep(30);               // pretend to use a scarce resource
                        inside.decrementAndGet();
                    } finally {
                        sem.release();                  // ALWAYS release in finally
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        for (Thread t : workers) t.join();
        System.out.println("1 limit: 10 workers, 3 permits -> max inside at once = " + maxInside.get()
                + ", permits afterwards = " + sem.availablePermits());
    }

    /** 2) A semaphore remembers a release that happened before anybody waited: no missed signal. */
    static void signalBeforeWait() throws InterruptedException {
        CountingSemaphore ready = new CountingSemaphore(0);
        ready.release();                                    // signal FIRST
        long t0 = System.nanoTime();
        Thread waiter = Thread.ofPlatform().start(() -> {
            try {
                ready.acquire();                            // ...wait LATER: still succeeds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.join(1_000);
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        System.out.println("2 signal before wait: waiter finished = " + !waiter.isAlive() + ", quickly (< 500 ms) = " + (ms < 500));
    }

    /** 3) tryAcquire with a timeout gives up instead of waiting forever. */
    static void timedTryAcquire() throws InterruptedException {
        CountingSemaphore sem = new CountingSemaphore(1);
        sem.acquire();                                      // main holds the only permit
        boolean[] got = new boolean[1];
        long[] waitedMs = new long[1];
        Thread t = Thread.ofPlatform().start(() -> {
            long t0 = System.nanoTime();
            try {
                got[0] = sem.tryAcquire(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitedMs[0] = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        });
        t.join();                                           // join makes got[]/waitedMs[] visible
        System.out.println("3 tryAcquire(100 ms) while the permit is held -> " + got[0]
                + ", waited >= 100 ms: " + (waitedMs[0] >= 100) + ", tryAcquire() now -> " + sem.tryAcquire());
        sem.release();
    }

    /** 4) acquire(3) waits until three permits are available at the same time. */
    static void multiPermitAcquire() throws InterruptedException {
        CountingSemaphore sem = new CountingSemaphore(0);
        AtomicInteger releasesBeforeWake = new AtomicInteger(-1);
        AtomicInteger releases = new AtomicInteger();
        Thread big = Thread.ofPlatform().start(() -> {
            try {
                sem.acquire(3);
                releasesBeforeWake.set(releases.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        for (int i = 0; i < 3; i++) {
            Thread.sleep(40);
            releases.incrementAndGet();                     // count first, then release
            sem.release();
        }
        big.join();
        System.out.println("4 acquire(3): woke after " + releasesBeforeWake.get() + " single releases, permits left = "
                + sem.availablePermits());
    }

    /** 5) A bounded semaphore treats "more releases than acquires" as a bug. */
    static void boundedRelease() {
        CountingSemaphore sem = new CountingSemaphore(2, 2);
        try {
            sem.release();
            System.out.println("5 bounded: extra release accepted?!");
        } catch (IllegalStateException e) {
            System.out.println("5 bounded: extra release -> IllegalStateException: " + e.getMessage());
        }
    }

    /** 6) A thread blocked in acquire() leaves on interrupt and takes no permit. */
    static void interruptWhileWaiting() throws InterruptedException {
        CountingSemaphore sem = new CountingSemaphore(0);
        String[] outcome = {"still waiting"};
        Thread t = Thread.ofPlatform().start(() -> {
            try {
                sem.acquire();
                outcome[0] = "acquired?!";
            } catch (InterruptedException e) {
                outcome[0] = "InterruptedException";
            }
        });
        Thread.sleep(50);
        t.interrupt();
        t.join();
        sem.release();
        System.out.println("6 interrupt while waiting -> " + outcome[0] + ", permits after one release = " + sem.availablePermits());
    }

    /** 7) FairSemaphore: waiters are served strictly in arrival order. */
    static void fairOrder() throws InterruptedException {
        FairSemaphore sem = new FairSemaphore(0);
        List<Integer> order = new CopyOnWriteArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int id = i;
            Thread t = Thread.ofPlatform().name("fair-" + i).start(() -> {
                try {
                    sem.acquire();
                    order.add(id);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            spinUntil(() -> t.getState() == Thread.State.WAITING, t.getName() + " queued");   // queued before the next starts
            threads.add(t);
        }
        for (int i = 1; i <= 5; i++) {
            sem.release();                                  // one permit at a time...
            final int taken = i;
            spinUntil(() -> order.size() >= taken, "permit " + taken + " taken");   // ...and wait until it was taken
        }
        for (Thread t : threads) t.join();
        System.out.println("7 fair semaphore: arrival order 0 1 2 3 4 -> served order " + order);
    }

    /** Spin until cond holds; fail loudly after 5 s instead of hanging the demo. */
    static void spinUntil(java.util.function.BooleanSupplier cond, String what) {
        long end = System.nanoTime() + 5_000_000_000L;
        while (!cond.getAsBoolean()) {
            if (System.nanoTime() - end > 0) throw new IllegalStateException("timed out waiting for: " + what);
            Thread.onSpinWait();
        }
    }
}
