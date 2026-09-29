package ch15.jmm;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * The classic "store buffering" litmus test, run millions of times. For each slot i:
 *   thread 1:  x[i] = 1;  r1[i] = y[i];        thread 2:  y[i] = 1;  r2[i] = x[i];
 * In ANY interleaving of these four statements at least one read sees a 1, so (r1, r2) = (0, 0)
 * is impossible if the program runs in source order. Plain fields: the JMM allows (0, 0), and real
 * CPUs/JITs produce it. Volatile-style accesses (AtomicIntegerArray get/set): forbidden.
 */
public class ReorderingDemo {
    static final int BATCH = 10_000, BATCHES = 5_000;        // 50 million trials

    public static void main(String[] args) throws Exception {
        System.out.printf("plain arrays:          (0,0) seen %,d times in %,d trials   [nondeterministic - varies run to run; any value > 0 proves reordering]%n",
                plain(), (long) BATCH * BATCHES);
        System.out.printf("AtomicIntegerArray:    (0,0) seen %,d times in %,d trials   [always 0 - forbidden by the JMM]%n",
                atomic(), (long) BATCH * BATCHES);
    }

    static long plain() throws Exception {
        int[] x = new int[BATCH], y = new int[BATCH], r1 = new int[BATCH], r2 = new int[BATCH];
        long[] count = {0};
        CyclicBarrier start = new CyclicBarrier(2);
        CyclicBarrier done = new CyclicBarrier(2, () -> {       // barrier action: tally and reset (runs between batches)
            for (int i = 0; i < BATCH; i++) { if (r1[i] == 0 && r2[i] == 0) count[0]++; x[i] = 0; y[i] = 0; }
        });
        runPair(start, done,
                () -> { for (int i = 0; i < BATCH; i++) { x[i] = 1; r1[i] = y[i]; } },
                () -> { for (int i = 0; i < BATCH; i++) { y[i] = 1; r2[i] = x[i]; } });
        return count[0];
    }

    static long atomic() throws Exception {
        AtomicIntegerArray x = new AtomicIntegerArray(BATCH), y = new AtomicIntegerArray(BATCH);
        int[] r1 = new int[BATCH], r2 = new int[BATCH];
        long[] count = {0};
        CyclicBarrier start = new CyclicBarrier(2);
        CyclicBarrier done = new CyclicBarrier(2, () -> {
            for (int i = 0; i < BATCH; i++) { if (r1[i] == 0 && r2[i] == 0) count[0]++; x.set(i, 0); y.set(i, 0); }
        });
        runPair(start, done,
                () -> { for (int i = 0; i < BATCH; i++) { x.set(i, 1); r1[i] = y.get(i); } },
                () -> { for (int i = 0; i < BATCH; i++) { y.set(i, 1); r2[i] = x.get(i); } });
        return count[0];
    }

    static void runPair(CyclicBarrier start, CyclicBarrier done, Runnable a, Runnable b) throws InterruptedException {
        Thread t1 = new Thread(() -> loop(start, done, a)), t2 = new Thread(() -> loop(start, done, b));
        t1.start(); t2.start(); t1.join(); t2.join();
    }

    static void loop(CyclicBarrier start, CyclicBarrier done, Runnable body) {
        try {
            for (int b = 0; b < BATCHES; b++) { start.await(); body.run(); done.await(); }
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
