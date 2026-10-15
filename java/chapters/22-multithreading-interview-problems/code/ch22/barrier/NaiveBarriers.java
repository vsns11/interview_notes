package ch22.barrier;

/** Two tempting barriers that are wrong. Kept small so the bug is easy to see. */
public final class NaiveBarriers {
    private NaiveBarriers() {}

    /** Bug A: never resets. Round 1 works; from round 2 on, count >= parties so nobody waits. */
    public static final class NoReset {
        private final int parties;
        private int count;

        public NoReset(int parties) { this.parties = parties; }

        public synchronized void await() throws InterruptedException {
            count++;
            if (count == parties) {
                notifyAll();
            } else {
                while (count < parties) wait();
            }
        }
    }

    /** Bug B: the last thread resets count to 0 BEFORE the sleepers re-check "count < parties". */
    public static final class ResetTooEarly {
        private final int parties;
        private int count;

        public ResetTooEarly(int parties) { this.parties = parties; }

        public synchronized void await() throws InterruptedException {
            count++;
            if (count == parties) {
                count = 0;              // ready for the next round...
                notifyAll();
            } else {
                while (count < parties) wait();   // ...but woken threads see 0 < parties and sleep again
            }
        }
    }
}
