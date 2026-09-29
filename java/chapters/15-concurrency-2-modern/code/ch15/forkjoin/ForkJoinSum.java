package ch15.forkjoin;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/** Divide and conquer on the Fork/Join framework: sum a big array. */
public class ForkJoinSum {

    static class SumTask extends RecursiveTask<Long> {
        static final int THRESHOLD = 10_000;           // below this, just loop
        final long[] data; final int from, to;

        SumTask(long[] data, int from, int to) { this.data = data; this.from = from; this.to = to; }

        @Override protected Long compute() {
            if (to - from <= THRESHOLD) {              // base case: small enough
                long s = 0;
                for (int i = from; i < to; i++) s += data[i];
                return s;
            }
            int mid = (from + to) >>> 1;
            SumTask left = new SumTask(data, from, mid);
            SumTask right = new SumTask(data, mid, to);
            left.fork();                               // push left half to this worker's deque (others may steal it)
            long r = right.compute();                  // work on the right half ourselves
            long l = left.join();                      // then wait for (or run) the left half
            return l + r;
        }
    }

    public static void main(String[] args) {
        long[] data = new long[20_000_000];                         // 160 MB of longs: 1, 2, 3, ...
        for (int i = 0; i < data.length; i++) data[i] = i + 1;
        long expected = 20_000_000L * 20_000_001L / 2;

        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("common pool parallelism = " + pool.getParallelism() + "   [machine-dependent: cores - 1]");

        for (int round = 1; round <= 3; round++) {    // first rounds include JIT warm-up
            long t0 = System.nanoTime();
            long seq = 0; for (long v : data) seq += v;
            long t1 = System.nanoTime();
            long fj = pool.invoke(new SumTask(data, 0, data.length));
            long t2 = System.nanoTime();
            long ps = Arrays.stream(data).parallel().sum();      // parallel streams use the same framework
            long t3 = System.nanoTime();
            System.out.printf("round %d: correct=%b  sequential %d ms, fork/join %d ms, parallel stream %d ms   [timings machine-dependent]%n",
                    round, seq == expected && fj == expected && ps == expected,
                    (t1 - t0) / 1_000_000, (t2 - t1) / 1_000_000, (t3 - t2) / 1_000_000);
        }
        System.out.println("tasks stolen between workers so far > 0: " + (pool.getStealCount() > 0));
    }
}
