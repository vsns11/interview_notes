package ch22.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final int PARTIES = 3;
    static final int ROUNDS = 4;

    public static void main(String[] args) throws Exception {
        correctReuse();
        noResetBug();
        resetTooEarlyBug();
        interruptedWaiterWithdraws();
    }

    /** 1) The generation barrier, reused for 4 rounds by 3 threads that arrive at random times. */
    static void correctReuse() throws InterruptedException {
        AtomicInteger[] arrivedInRound = new AtomicInteger[ROUNDS];
        for (int r = 0; r < ROUNDS; r++) arrivedInRound[r] = new AtomicInteger();
        AtomicInteger round = new AtomicInteger();
        AtomicInteger violations = new AtomicInteger();
        ReusableBarrier barrier = new ReusableBarrier(PARTIES, () ->
                System.out.println("1 barrier action: round " + round.getAndIncrement() + " complete, all "
                        + PARTIES + " arrived"));
        List<Thread> workers = new ArrayList<>();
        for (int w = 0; w < PARTIES; w++) {
            workers.add(Thread.ofPlatform().name("worker-" + w).start(() -> {
                try {
                    for (int r = 0; r < ROUNDS; r++) {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(30));   // uneven work per round
                        arrivedInRound[r].incrementAndGet();
                        barrier.await();
                        // Past the barrier: every party must have arrived in this round.
                        if (arrivedInRound[r].get() != PARTIES) violations.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        for (Thread t : workers) t.join();
        System.out.println("1 generation barrier: " + ROUNDS + " rounds, threads that left a round early = " + violations.get());
    }

    /** 2) No reset: the second round lets everyone straight through. */
    static void noResetBug() throws InterruptedException {
        NaiveBarriers.NoReset barrier = new NaiveBarriers.NoReset(PARTIES);
        AtomicBoolean slowArrivedRound2 = new AtomicBoolean();
        AtomicInteger passedEarly = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        for (int w = 0; w < PARTIES; w++) {
            final boolean slow = (w == PARTIES - 1);
            threads.add(Thread.ofPlatform().start(() -> {
                try {
                    barrier.await();                          // round 1: fine
                    if (slow) {
                        Thread.sleep(200);                    // the slow thread is late for round 2
                        slowArrivedRound2.set(true);
                    }
                    barrier.await();                          // round 2: should wait for the slow one
                    if (!slow && !slowArrivedRound2.get()) passedEarly.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        for (Thread t : threads) t.join();
        System.out.println("2 NoReset: round 2, fast threads that passed before the slow one arrived = "
                + passedEarly.get() + " of " + (PARTIES - 1) + "  (BUG)");
    }

    /** 3) Resetting the count inside the "last arriver" branch strands the sleepers. */
    static void resetTooEarlyBug() throws InterruptedException {
        NaiveBarriers.ResetTooEarly barrier = new NaiveBarriers.ResetTooEarly(PARTIES);
        List<Thread> threads = new ArrayList<>();
        for (int w = 0; w < PARTIES; w++) {
            Thread t = Thread.ofPlatform().daemon(true).unstarted(() -> {   // daemon: may stay stuck, JVM still exits
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.start();
            threads.add(t);
        }
        Thread.sleep(300);
        long stuck = threads.stream().filter(Thread::isAlive).count();
        System.out.println("3 ResetTooEarly: round 1, threads still stuck after 300 ms = " + stuck + " of " + PARTIES + "  (BUG)");
        for (Thread t : threads) t.interrupt();                // tidy up
    }

    /** 4) An interrupted waiter withdraws its arrival; the barrier keeps working. */
    static void interruptedWaiterWithdraws() throws InterruptedException {
        ReusableBarrier barrier = new ReusableBarrier(PARTIES, null);
        String[] outcome = {"?"};
        Thread quitter = Thread.ofPlatform().start(() -> {
            try {
                barrier.await();
                outcome[0] = "passed?!";
            } catch (InterruptedException e) {
                outcome[0] = "InterruptedException";
            }
        });
        spinUntil(() -> barrier.waitingNow() == 1, "quitter waiting at the barrier");
        quitter.interrupt();
        quitter.join();
        int afterWithdraw = barrier.waitingNow();
        List<Thread> fresh = new ArrayList<>();
        for (int w = 0; w < PARTIES; w++) {
            fresh.add(Thread.ofPlatform().start(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        for (Thread t : fresh) t.join();
        System.out.println("4 interrupted waiter -> " + outcome[0] + ", waiting count after withdraw = " + afterWithdraw
                + ", next full round completed = true");
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
