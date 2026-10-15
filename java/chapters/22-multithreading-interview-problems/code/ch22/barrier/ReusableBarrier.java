package ch22.barrier;

/**
 * A reusable (cyclic) barrier: await() blocks until `parties` threads have called it,
 * then all are released and the barrier is immediately ready for the next round.
 *
 * The trick is the generation number. A waiter remembers the generation it arrived in
 * and waits until that generation is over; it does NOT look at the arrival count, which
 * the last thread has already reset for the next round.
 */
public final class ReusableBarrier {
    private final int parties;
    private final Runnable barrierAction;   // run by the last arriver, before anyone is released
    private int arrived;                    // arrivals in the current generation
    private long generation;                // bumps once per completed round

    public ReusableBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException("parties must be > 0");
        this.parties = parties;
        this.barrierAction = barrierAction;
    }

    /** Returns this thread's arrival index (parties-1 for the first, 0 for the last), like CyclicBarrier. */
    public synchronized int await() throws InterruptedException {
        final long myGeneration = generation;
        int index = parties - 1 - arrived;
        arrived++;
        if (arrived == parties) {           // last one in trips the barrier
            if (barrierAction != null) barrierAction.run();
            arrived = 0;                    // reset for the next round...
            generation++;                   // ...and end this one
            notifyAll();
            return 0;
        }
        try {
            while (myGeneration == generation) {   // not "arrived < parties"!
                wait();
            }
        } catch (InterruptedException e) {
            if (myGeneration == generation) {       // still waiting: withdraw our arrival
                arrived--;
                throw e;
            }
            Thread.currentThread().interrupt();     // the round already completed: keep the interrupt, return normally
        }
        return index;
    }

    public synchronized int waitingNow() {
        return arrived;
    }
}
