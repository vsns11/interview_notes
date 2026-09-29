package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Heaps: k-th largest, top-k frequent, merge k sorted arrays, two heaps (running median, maximise capital). */
public class HeapTopK {
    /** k-th largest: keep a MIN-heap of the k largest seen; its root is the answer. O(n log k). */
    static int kthLargest(int[] a, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int x : a) {
            heap.offer(x);
            if (heap.size() > k) heap.poll();          // evict the smallest
        }
        return heap.peek();
    }

    /** k most frequent values, most frequent first (ties: smaller value first). */
    static List<Integer> topKFrequent(int[] a, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int x : a) freq.merge(x, 1, Integer::sum);
        // min-heap ordered by (frequency asc, value desc) so the "worst" candidate is on top
        PriorityQueue<Map.Entry<Integer, Integer>> heap = new PriorityQueue<>(
                Map.Entry.<Integer, Integer>comparingByValue()
                        .thenComparing(Map.Entry.<Integer, Integer>comparingByKey().reversed()));
        for (var e : freq.entrySet()) {
            heap.offer(e);
            if (heap.size() > k) heap.poll();
        }
        List<Integer> out = new ArrayList<>();
        while (!heap.isEmpty()) out.add(heap.poll().getKey());
        Collections.reverse(out);
        return out;
    }

    /** Merge k sorted arrays: the heap holds one {value, arrayIndex, elementIndex} per array. */
    static List<Integer> mergeSorted(int[][] arrays) {
        PriorityQueue<int[]> heap = new PriorityQueue<>(Comparator.comparingInt(x -> x[0]));
        for (int i = 0; i < arrays.length; i++) if (arrays[i].length > 0) heap.offer(new int[]{arrays[i][0], i, 0});
        List<Integer> out = new ArrayList<>();
        while (!heap.isEmpty()) {
            int[] top = heap.poll();
            out.add(top[0]);
            int arr = top[1], next = top[2] + 1;
            if (next < arrays[arr].length) heap.offer(new int[]{arrays[arr][next], arr, next});
        }
        return out;
    }

    /** Running median with two heaps: max-heap for the lower half, min-heap for the upper half. */
    static final class MedianFinder {
        private final PriorityQueue<Integer> low = new PriorityQueue<>(Comparator.reverseOrder());
        private final PriorityQueue<Integer> high = new PriorityQueue<>();

        void add(int x) {
            low.offer(x);
            high.offer(low.poll());                    // move the largest of low to high
            if (high.size() > low.size()) low.offer(high.poll());   // keep low >= high in size
        }

        double median() {
            return low.size() > high.size() ? low.peek() : (low.peek() + (long) high.peek()) / 2.0;
        }
    }

    /**
     * Two heaps, second shape: pick at most k projects to maximise capital. A project needs capital[i] to start
     * and adds profit[i]. Min-heap by required capital = "locked"; max-heap by profit = "affordable now".
     */
    static long maximizeCapital(int k, long start, int[] profit, int[] capital) {
        PriorityQueue<Integer> locked = new PriorityQueue<>(Comparator.comparingInt(i -> capital[i]));
        PriorityQueue<Integer> affordable = new PriorityQueue<>(Comparator.comparingInt((Integer i) -> profit[i]).reversed());
        for (int i = 0; i < profit.length; i++) locked.offer(i);
        long money = start;
        for (int round = 0; round < k; round++) {
            while (!locked.isEmpty() && capital[locked.peek()] <= money) affordable.offer(locked.poll()); // unlock
            if (affordable.isEmpty()) break;                   // nothing we can start
            money += profit[affordable.poll()];                // greedy: the most profitable affordable project
        }
        return money;
    }

    public static void main(String[] args) {
        eq(kthLargest(new int[]{3, 2, 1, 5, 6, 4}, 2), 5, "kthLargest");
        eq(topKFrequent(new int[]{1, 1, 1, 2, 2, 3, 4, 4}, 2), List.of(1, 2), "topKFrequent");
        eq(mergeSorted(new int[][]{{1, 4, 7}, {2, 5}, {}, {0, 9}}), List.of(0, 1, 2, 4, 5, 7, 9), "mergeSorted");
        MedianFinder mf = new MedianFinder();
        mf.add(5); mf.add(15);
        eq(mf.median(), 10.0, "median of 5,15");
        mf.add(1);
        eq(mf.median(), 5.0, "median of 5,15,1");
        eq(maximizeCapital(2, 0, new int[]{1, 2, 3}, new int[]{0, 1, 1}), 4L, "maximizeCapital k=2");
        eq(maximizeCapital(3, 0, new int[]{1, 2, 3}, new int[]{0, 1, 2}), 6L, "maximizeCapital k=3");
        eq(maximizeCapital(1, 0, new int[]{5}, new int[]{1}), 0L, "maximizeCapital nothing affordable");
        done();
    }
}
