package ch22.mergesort;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/** Merge sort three ways: one thread per split, threads only near the top, and Fork/Join. */
public final class ThreadMergeSort {
    private ThreadMergeSort() {}

    /** Counts platform threads created, so the demo can show the difference. */
    public static final AtomicInteger THREADS_CREATED = new AtomicInteger();

    /** Merges sorted a[lo,mid) and a[mid,hi) using buf[lo,hi) as scratch space. */
    static void merge(int[] a, int[] buf, int lo, int mid, int hi) {
        System.arraycopy(a, lo, buf, lo, hi - lo);
        int i = lo, j = mid, k = lo;
        while (i < mid && j < hi) a[k++] = buf[i] <= buf[j] ? buf[i++] : buf[j++];  // <= keeps it stable
        while (i < mid) a[k++] = buf[i++];
        // the rest of the right half is already in place
    }

    // ---------- 1) Naive: a new thread for EVERY split, down to single elements ----------
    public static void naive(int[] a) throws InterruptedException {
        naive(a, new int[a.length], 0, a.length);
    }

    private static void naive(int[] a, int[] buf, int lo, int hi) throws InterruptedException {
        if (hi - lo < 2) return;
        int mid = (lo + hi) >>> 1;                       // >>> 1: no int overflow for huge lo + hi
        Thread left = new Thread(() -> {
            try { naive(a, buf, lo, mid); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        THREADS_CREATED.incrementAndGet();
        left.start();
        naive(a, buf, mid, hi);                          // the current thread sorts the right half itself
        left.join();                                     // join: the left half's writes are now visible
        merge(a, buf, lo, mid, hi);
    }

    // ---------- 2) Depth cutoff: threads only for the top levels, then sequential ----------
    public static void withCutoff(int[] a, int maxDepth) throws InterruptedException {
        withCutoff(a, new int[a.length], 0, a.length, maxDepth);
    }

    private static void withCutoff(int[] a, int[] buf, int lo, int hi, int depth) throws InterruptedException {
        if (depth == 0 || hi - lo < 2) {
            Arrays.sort(a, lo, hi);                      // below the cutoff: plain sequential sort
            return;
        }
        int mid = (lo + hi) >>> 1;
        Throwable[] failure = new Throwable[1];
        Thread left = new Thread(() -> {
            try { withCutoff(a, buf, lo, mid, depth - 1); }
            catch (Throwable t) { failure[0] = t; }      // an exception in a thread is otherwise lost
        });
        THREADS_CREATED.incrementAndGet();
        left.start();
        withCutoff(a, buf, mid, hi, depth - 1);
        left.join();
        if (failure[0] != null) throw new IllegalStateException("left half failed", failure[0]);
        merge(a, buf, lo, mid, hi);
    }

    // ---------- 3) Fork/Join: tasks, not threads; a fixed pool with work stealing ----------
    public static void forkJoin(int[] a, ForkJoinPool pool) {
        pool.invoke(new SortTask(a, new int[a.length], 0, a.length));
    }

    private static final class SortTask extends RecursiveAction {
        private static final long serialVersionUID = 1L;
        private static final int SEQUENTIAL_BELOW = 1 << 13;
        private final int[] a, buf;
        private final int lo, hi;

        SortTask(int[] a, int[] buf, int lo, int hi) { this.a = a; this.buf = buf; this.lo = lo; this.hi = hi; }

        @Override protected void compute() {
            if (hi - lo <= SEQUENTIAL_BELOW) { Arrays.sort(a, lo, hi); return; }
            int mid = (lo + hi) >>> 1;
            invokeAll(new SortTask(a, buf, lo, mid), new SortTask(a, buf, mid, hi));
            merge(a, buf, lo, mid, hi);                  // exceptions from subtasks propagate here
        }
    }
}
