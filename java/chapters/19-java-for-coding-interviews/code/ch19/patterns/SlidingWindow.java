package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Sliding window: fixed size, and variable size (grow right, shrink left while invalid). */
public class SlidingWindow {
    /** Largest sum of any k consecutive elements. O(n). */
    static long maxSumOfK(int[] a, int k) {
        long window = 0, best;
        for (int i = 0; i < k; i++) window += a[i];
        best = window;
        for (int right = k; right < a.length; right++) {
            window += a[right] - a[right - k];         // slide: add new, drop oldest
            best = Math.max(best, window);
        }
        return best;
    }

    /** Length of the shortest subarray with sum >= target (positive numbers), or 0. */
    static int shortestWithSumAtLeast(int[] a, int target) {
        int left = 0, sum = 0, best = Integer.MAX_VALUE;
        for (int right = 0; right < a.length; right++) {
            sum += a[right];                           // grow
            while (sum >= target) {                    // valid: record, then shrink
                best = Math.min(best, right - left + 1);
                sum -= a[left++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    /** Longest substring without repeating characters. last-seen index lets left jump. */
    static int longestUniqueSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int left = 0, best = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            Integer prev = lastSeen.put(c, right);
            if (prev != null && prev >= left) left = prev + 1;   // jump past the duplicate
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    /** Longest substring with at most k distinct characters: counts in an int[128] for ASCII. */
    static int longestWithKDistinct(String s, int k) {
        int[] count = new int[128];
        int distinct = 0, left = 0, best = 0;
        for (int right = 0; right < s.length(); right++) {
            if (count[s.charAt(right)]++ == 0) distinct++;
            while (distinct > k) {
                if (--count[s.charAt(left++)] == 0) distinct--;
            }
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    public static void main(String[] args) {
        eq(maxSumOfK(new int[]{2, 1, 5, 1, 3, 2}, 3), 9L, "max sum of 3");
        eq(shortestWithSumAtLeast(new int[]{2, 3, 1, 2, 4, 3}, 7), 2, "shortest sum>=7");
        eq(shortestWithSumAtLeast(new int[]{1, 1}, 5), 0, "shortest none");
        eq(longestUniqueSubstring("abcabcbb"), 3, "unique abcabcbb");
        eq(longestUniqueSubstring("abba"), 2, "unique abba");
        eq(longestWithKDistinct("eceba", 2), 3, "2 distinct eceba");
        done();
    }
}
