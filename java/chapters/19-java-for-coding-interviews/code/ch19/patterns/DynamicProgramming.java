package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Dynamic programming: memoisation (top-down) vs tabulation (bottom-up), 1D and 2D. */
public class DynamicProgramming {
    // ---- 1D: ways to climb n stairs taking 1 or 2 steps ----
    /** Top-down: plain recursion + a cache. long[] with 0 meaning "not computed". */
    static long climbMemo(int n, long[] memo) {
        if (n <= 1) return 1;
        if (memo[n] != 0) return memo[n];
        return memo[n] = climbMemo(n - 1, memo) + climbMemo(n - 2, memo);
    }

    /** Bottom-up with O(1) space: only the last two answers are needed. */
    static long climbTab(int n) {
        long prev2 = 1, prev1 = 1;                   // ways(0), ways(1)
        for (int i = 2; i <= n; i++) { long cur = prev1 + prev2; prev2 = prev1; prev1 = cur; }
        return prev1;
    }

    /** House robber: max sum of non-adjacent elements. dp[i] = max(dp[i-1], dp[i-2] + a[i]). */
    static int rob(int[] a) {
        int skip = 0, take = 0;                      // best ending two back / one back
        for (int x : a) { int next = Math.max(take, skip + x); skip = take; take = next; }
        return take;
    }

    /** Coin change: fewest coins summing to amount (unbounded coins), or -1. */
    static int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, Integer.MAX_VALUE);
        dp[0] = 0;
        for (int x = 1; x <= amount; x++)
            for (int c : coins)
                if (c <= x && dp[x - c] != Integer.MAX_VALUE)   // guard: MAX_VALUE + 1 would overflow
                    dp[x] = Math.min(dp[x], dp[x - c] + 1);
        return dp[amount] == Integer.MAX_VALUE ? -1 : dp[amount];
    }

    /** Longest increasing subsequence, O(n log n): tails[i] = smallest tail of an increasing run of length i+1. */
    static int lis(int[] a) {
        int[] tails = new int[a.length];
        int len = 0;
        for (int x : a) {
            int i = Arrays.binarySearch(tails, 0, len, x);
            if (i < 0) i = -i - 1;                   // insertion point
            tails[i] = x;
            if (i == len) len++;
        }
        return len;
    }

    // ---- 2D ----
    /** Longest common subsequence: dp[i][j] = LCS of a[0..i) and b[0..j). */
    static int lcs(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++)
            for (int j = 1; j <= b.length(); j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1] + 1
                        : Math.max(dp[i - 1][j], dp[i][j - 1]);
        return dp[a.length()][b.length()];
    }

    /** Edit distance (insert, delete, replace), top-down with a HashMap memo keyed by a record. */
    record Key(int i, int j) {}

    static int editDistance(String a, String b, int i, int j, Map<Key, Integer> memo) {
        if (i == a.length()) return b.length() - j;
        if (j == b.length()) return a.length() - i;
        Key k = new Key(i, j);
        Integer cached = memo.get(k);
        if (cached != null) return cached;
        int best = a.charAt(i) == b.charAt(j)
                ? editDistance(a, b, i + 1, j + 1, memo)
                : 1 + Math.min(editDistance(a, b, i + 1, j + 1, memo),        // replace
                        Math.min(editDistance(a, b, i + 1, j, memo),          // delete
                                editDistance(a, b, i, j + 1, memo)));         // insert
        memo.put(k, best);
        return best;
    }

    /** 0/1 knapsack in one row: iterate capacity DOWNWARDS so each item is used at most once. */
    static int knapsack(int[] weight, int[] value, int capacity) {
        int[] dp = new int[capacity + 1];
        for (int item = 0; item < weight.length; item++)
            for (int c = capacity; c >= weight[item]; c--)
                dp[c] = Math.max(dp[c], dp[c - weight[item]] + value[item]);
        return dp[capacity];
    }

    /** Unique paths in an m x n grid moving only right/down. */
    static long gridPaths(int m, int n) {
        long[] row = new long[n];
        Arrays.fill(row, 1);
        for (int r = 1; r < m; r++) for (int c = 1; c < n; c++) row[c] += row[c - 1];
        return row[n - 1];
    }

    public static void main(String[] args) {
        eq(climbMemo(50, new long[51]), 20365011074L, "climb memo n=50");
        eq(climbTab(50), 20365011074L, "climb tab n=50");
        eq(rob(new int[]{2, 7, 9, 3, 1}), 12, "house robber");
        eq(coinChange(new int[]{1, 5, 11}, 15), 3, "coin change 15");
        eq(coinChange(new int[]{2}, 3), -1, "coin change impossible");
        eq(lis(new int[]{10, 9, 2, 5, 3, 7, 101, 18}), 4, "LIS");
        eq(lcs("abcde", "ace"), 3, "LCS");
        eq(editDistance("horse", "ros", 0, 0, new HashMap<>()), 3, "edit distance");
        eq(knapsack(new int[]{1, 3, 4, 5}, new int[]{1, 4, 5, 7}, 7), 9, "0/1 knapsack");
        eq(gridPaths(3, 7), 28L, "grid paths");
        done();
    }
}
