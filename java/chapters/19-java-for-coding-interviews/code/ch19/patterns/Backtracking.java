package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Backtracking: choose, explore, un-choose. Copy the path when you record it. */
public class Backtracking {
    static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        subsets(nums, 0, new ArrayList<>(), out);
        return out;
    }

    private static void subsets(int[] nums, int start, List<Integer> path, List<List<Integer>> out) {
        out.add(new ArrayList<>(path));                   // COPY: path keeps changing
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);                            // choose
            subsets(nums, i + 1, path, out);              // explore
            path.removeLast();                            // un-choose (Java 21; or remove(size()-1))
        }
    }

    static List<List<Integer>> permutations(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        permute(nums, new boolean[nums.length], new ArrayList<>(), out);
        return out;
    }

    private static void permute(int[] nums, boolean[] used, List<Integer> path, List<List<Integer>> out) {
        if (path.size() == nums.length) { out.add(List.copyOf(path)); return; }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true; path.add(nums[i]);
            permute(nums, used, path, out);
            used[i] = false; path.removeLast();
        }
    }

    /** Combination sum: candidates may be reused; sorted input lets us stop early (pruning). */
    static List<List<Integer>> combinationSum(int[] candidates, int target) {
        int[] c = candidates.clone();
        Arrays.sort(c);
        List<List<Integer>> out = new ArrayList<>();
        combo(c, target, 0, new ArrayList<>(), out);
        return out;
    }

    private static void combo(int[] c, int remaining, int start, List<Integer> path, List<List<Integer>> out) {
        if (remaining == 0) { out.add(List.copyOf(path)); return; }
        for (int i = start; i < c.length && c[i] <= remaining; i++) {   // prune: sorted, so stop
            path.add(c[i]);
            combo(c, remaining - c[i], i, path, out);                    // i, not i+1: reuse allowed
            path.removeLast();
        }
    }

    /** N-Queens count: one queen per row; sets of used columns and diagonals give O(1) checks. */
    static int nQueens(int n) {
        return place(n, 0, new boolean[n], new boolean[2 * n], new boolean[2 * n]);
    }

    private static int place(int n, int row, boolean[] cols, boolean[] diag, boolean[] anti) {
        if (row == n) return 1;
        int count = 0;
        for (int c = 0; c < n; c++) {
            int d = row - c + n, a = row + c;
            if (cols[c] || diag[d] || anti[a]) continue;
            cols[c] = diag[d] = anti[a] = true;
            count += place(n, row + 1, cols, diag, anti);
            cols[c] = diag[d] = anti[a] = false;
        }
        return count;
    }

    public static void main(String[] args) {
        eq(subsets(new int[]{1, 2, 3}).toString(), "[[], [1], [1, 2], [1, 2, 3], [1, 3], [2], [2, 3], [3]]", "subsets");
        eq(permutations(new int[]{1, 2, 3}).size(), 6, "permutations count");
        eq(permutations(new int[]{1, 2}).toString(), "[[1, 2], [2, 1]]", "permutations of 2");
        eq(combinationSum(new int[]{2, 3, 6, 7}, 7).toString(), "[[2, 2, 3], [7]]", "combination sum");
        eq(nQueens(8), 92, "8 queens");
        done();
    }
}
