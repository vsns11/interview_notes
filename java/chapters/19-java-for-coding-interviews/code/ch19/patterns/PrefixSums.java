package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Prefix sums: precompute running totals so any range sum is one subtraction. */
public class PrefixSums {
    /** prefix[i] = sum of a[0..i-1]; prefix has length n+1 so prefix[0] = 0. */
    static long[] prefix(int[] a) {
        long[] p = new long[a.length + 1];
        for (int i = 0; i < a.length; i++) p[i + 1] = p[i] + a[i];
        return p;
    }

    /** Sum of a[from..to] inclusive in O(1). */
    static long rangeSum(long[] p, int from, int to) {
        return p[to + 1] - p[from];
    }

    /** Number of subarrays summing to k (negatives allowed): count earlier prefixes equal to sum - k. */
    static int subarraysSumToK(int[] a, int k) {
        Map<Integer, Integer> seen = new HashMap<>();
        seen.put(0, 1);                                // empty prefix
        int sum = 0, count = 0;
        for (int x : a) {
            sum += x;
            count += seen.getOrDefault(sum - k, 0);
            seen.merge(sum, 1, Integer::sum);
        }
        return count;
    }

    /** 2D prefix sums: sum of any sub-rectangle in O(1). */
    static long[][] prefix2D(int[][] g) {
        int rows = g.length, cols = g[0].length;
        long[][] p = new long[rows + 1][cols + 1];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                p[r + 1][c + 1] = g[r][c] + p[r][c + 1] + p[r + 1][c] - p[r][c];
        return p;
    }

    static long rect(long[][] p, int r1, int c1, int r2, int c2) {
        return p[r2 + 1][c2 + 1] - p[r1][c2 + 1] - p[r2 + 1][c1] + p[r1][c1];
    }

    /** Difference array: add v to a[l..r] for many updates, then one prefix pass. */
    static int[] applyRangeAdds(int n, int[][] updates) {
        int[] diff = new int[n + 1];
        for (int[] u : updates) { diff[u[0]] += u[2]; diff[u[1] + 1] -= u[2]; }
        int[] out = new int[n];
        int run = 0;
        for (int i = 0; i < n; i++) { run += diff[i]; out[i] = run; }
        return out;
    }

    public static void main(String[] args) {
        long[] p = prefix(new int[]{3, 1, 4, 1, 5, 9});
        eq(rangeSum(p, 1, 3), 6L, "rangeSum 1..3");
        eq(rangeSum(p, 0, 5), 23L, "rangeSum all");
        eq(subarraysSumToK(new int[]{1, 2, 3, -2, 2}, 3), 4, "subarrays sum 3");
        long[][] p2 = prefix2D(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}});
        eq(rect(p2, 1, 1, 2, 2), 28L, "2D rect");
        eq(applyRangeAdds(5, new int[][]{{1, 3, 2}, {2, 4, 3}}), new int[]{0, 2, 5, 5, 3}, "difference array");
        done();
    }
}
