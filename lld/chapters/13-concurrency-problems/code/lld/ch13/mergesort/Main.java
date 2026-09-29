package lld.ch13.mergesort;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/** Multi-threaded merge sort with the Fork/Join framework (divide and conquer + work stealing). */
public class Main {

    static final class ParallelMergeSort extends RecursiveAction {
        private static final long serialVersionUID = 1L;
        static final int THRESHOLD = 1 << 13;   // below ~8K elements, splitting costs more than it saves
        private final int[] a, buf;             // buf = one shared scratch array; tasks use disjoint ranges
        private final int lo, hi;               // sorts a[lo, hi)

        ParallelMergeSort(int[] a, int[] buf, int lo, int hi) { this.a = a; this.buf = buf; this.lo = lo; this.hi = hi; }

        @Override protected void compute() {
            if (hi - lo <= THRESHOLD) {
                Arrays.sort(a, lo, hi);                           // sequential base case
                return;
            }
            int mid = (lo + hi) >>> 1;                            // overflow-safe midpoint
            invokeAll(new ParallelMergeSort(a, buf, lo, mid),     // fork both halves, wait for both
                      new ParallelMergeSort(a, buf, mid, hi));
            merge(mid);
        }

        private void merge(int mid) {
            if (a[mid - 1] <= a[mid]) return;                     // already in order: skip
            System.arraycopy(a, lo, buf, lo, hi - lo);
            int i = lo, j = mid, k = lo;
            while (i < mid && j < hi) a[k++] = buf[i] <= buf[j] ? buf[i++] : buf[j++];   // <= keeps it stable
            while (i < mid) a[k++] = buf[i++];
            // right-half leftovers are already in place
        }
    }

    static void parallelSort(int[] a) {
        ForkJoinPool.commonPool().invoke(new ParallelMergeSort(a, new int[a.length], 0, a.length));
    }

    public static void main(String[] args) {
        int[] small = {38, 27, 43, 3, 9, 82, 10, 3};
        parallelSort(small);
        System.out.println("small: " + Arrays.toString(small));

        int n = 4_000_000;
        int[] data = new Random(42).ints(n).toArray();
        int[] expected = data.clone();
        Arrays.sort(expected);

        int[] mine = data.clone();
        parallelSort(mine);
        System.out.println(n + " random ints: matches Arrays.sort = " + Arrays.equals(mine, expected));

        int[] jdk = data.clone();
        Arrays.parallelSort(jdk);                                  // the JDK's own parallel sort
        System.out.println("Arrays.parallelSort agrees        = " + Arrays.equals(jdk, expected));
    }
}
