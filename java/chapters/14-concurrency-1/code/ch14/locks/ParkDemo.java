package ch14.locks;

import java.util.concurrent.locks.LockSupport;

/** LockSupport: one permit per thread, unpark-before-park is remembered, interrupts wake park. */
public class ParkDemo {
    static long ms(long t0) { return (System.nanoTime() - t0) / 1_000_000; }

    /** Spin until t is parked; give up after 5 s instead of hanging the demo. */
    static void awaitParked(Thread t) {
        long end = System.nanoTime() + 5_000_000_000L;
        while (t.getState() != Thread.State.WAITING) {
            if (System.nanoTime() - end > 0) throw new IllegalStateException(t.getName() + " never parked");
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 1) unpark BEFORE park: the permit is stored, park() returns at once (no missed signal)
        Thread t = Thread.currentThread();
        LockSupport.unpark(t);
        long t0 = System.nanoTime();
        LockSupport.park();
        System.out.println("1 unpark then park: returned immediately: " + (ms(t0) < 50));

        // 2) permits do NOT accumulate: two unparks give one permit
        LockSupport.unpark(t); LockSupport.unpark(t);
        LockSupport.parkNanos(1);                                    // consumes the single permit
        t0 = System.nanoTime();
        LockSupport.parkNanos(300_000_000L);                         // no permit left -> waits ~300 ms
        System.out.println("2 second park after two unparks waited for the timeout: " + (ms(t0) >= 250));

        // 3) a parked thread, its blocker, and unpark from another thread
        Object blocker = new Object();
        boolean[] woke = {false};
        Thread sleeper = new Thread(() -> {
            LockSupport.park(blocker);                               // shows up in thread dumps
            woke[0] = true;
        }, "sleeper");
        sleeper.setDaemon(true);
        sleeper.start();
        awaitParked(sleeper);
        System.out.println("3 sleeper state = " + sleeper.getState()
                + ", blocker recorded: " + (LockSupport.getBlocker(sleeper) == blocker));
        LockSupport.unpark(sleeper);
        sleeper.join(5_000);
        System.out.println("3 after unpark: woke = " + woke[0]);

        // 4) interrupt also wakes park, WITHOUT an exception; the flag stays set
        boolean[] flag = {false};
        Thread victim = new Thread(() -> {
            LockSupport.park();
            flag[0] = Thread.currentThread().isInterrupted();
        });
        victim.setDaemon(true);
        victim.start();
        awaitParked(victim);
        victim.interrupt();
        victim.join(5_000);
        System.out.println("4 park returned on interrupt, no exception, interrupt flag still set: " + flag[0]);
    }
}
