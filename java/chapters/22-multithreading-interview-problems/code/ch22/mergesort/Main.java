package ch22.mergesort;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class Main {

    interface Sorter { void sort(int[] a) throws InterruptedException; }

    /** Sorts copies (best of 'repeats' runs), checks against Arrays.sort, prints threads created and time. */
    static void run(String name, int[] data, int[] expected, int repeats, Sorter s) throws InterruptedException {
        long ms = Long.MAX_VALUE;
        int[] a = null;
        for (int r = 0; r < repeats; r++) {
            a = data.clone();
            ThreadMergeSort.THREADS_CREATED.set(0);
            long t0 = System.nanoTime();
            s.sort(a);
            ms = Math.min(ms, (System.nanoTime() - t0) / 1_000_000);
        }
        System.out.printf("  %-24s correct=%-5b threads created=%-6d [machine-dependent] %d ms%n",
                name, Arrays.equals(a, expected), ThreadMergeSort.THREADS_CREATED.get(), ms);
    }

    public static void main(String[] args) throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        int depth = 32 - Integer.numberOfLeadingZeros(cores - 1);   // ceil(log2(cores)): 2^depth >= cores
        System.out.println("[machine-dependent] cores=" + cores + ", cutoff depth=" + depth);
        Random rnd = new Random(42);

        int[] small = rnd.ints(2_000).toArray();
        int[] smallSorted = small.clone();
        Arrays.sort(smallSorted);
        System.out.println("2,000 ints:");
        run("naive thread per split", small, smallSorted, 1, ThreadMergeSort::naive);
        run("depth cutoff", small, smallSorted, 1, a -> ThreadMergeSort.withCutoff(a, depth));

        int[] big = rnd.ints(4_000_000).toArray();
        int[] bigSorted = big.clone();
        Arrays.sort(bigSorted);
        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("4,000,000 ints (best of 5 runs each):");
        run("sequential Arrays.sort", big, bigSorted, 5, Arrays::sort);
        run("depth cutoff", big, bigSorted, 5, a -> ThreadMergeSort.withCutoff(a, depth));
        run("Fork/Join", big, bigSorted, 5, a -> ThreadMergeSort.forkJoin(a, pool));
    }
}
