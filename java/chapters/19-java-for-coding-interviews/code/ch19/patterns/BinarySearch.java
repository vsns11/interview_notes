package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Binary search on an index range, lower bound, rotated arrays, and binary search on the ANSWER. */
public class BinarySearch {
    /** Classic: index of target in sorted array, or -1. Invariant: target, if present, is in [lo, hi]. */
    static int search(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;              // never (lo + hi) / 2
            if (a[mid] == target) return mid;
            if (a[mid] < target) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }

    /** Lower bound: first index with a[i] >= target (a.length if none). Half-open range [lo, hi). */
    static int lowerBound(int[] a, int target) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (a[mid] < target) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    /** Search in a sorted array rotated at an unknown pivot, e.g. [4,5,6,7,0,1,2]. */
    static int searchRotated(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target) return mid;
            if (a[lo] <= a[mid]) {                     // left half is sorted
                if (a[lo] <= target && target < a[mid]) hi = mid - 1; else lo = mid + 1;
            } else {                                   // right half is sorted
                if (a[mid] < target && target <= a[hi]) lo = mid + 1; else hi = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Binary search on the answer: the smallest ship capacity that ships all packages within `days`.
     * feasible(capacity) is monotonic: false ... false true ... true. Find the first true.
     */
    static int minCapacity(int[] weights, int days) {
        int lo = Arrays.stream(weights).max().getAsInt();     // must fit the heaviest package
        int hi = Arrays.stream(weights).sum();                // everything in one day
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (daysNeeded(weights, mid) <= days) hi = mid; else lo = mid + 1;
        }
        return lo;
    }

    static int daysNeeded(int[] weights, int capacity) {
        int days = 1, load = 0;
        for (int w : weights) {
            if (load + w > capacity) { days++; load = 0; }
            load += w;
        }
        return days;
    }

    public static void main(String[] args) {
        int[] a = {1, 3, 5, 7, 9, 11};
        eq(search(a, 7), 3, "search hit");
        eq(search(a, 4), -1, "search miss");
        eq(lowerBound(new int[]{1, 2, 2, 2, 5}, 2), 1, "lowerBound first 2");
        eq(lowerBound(new int[]{1, 2, 2, 2, 5}, 6), 5, "lowerBound past end");
        eq(searchRotated(new int[]{4, 5, 6, 7, 0, 1, 2}, 0), 4, "rotated");
        eq(minCapacity(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 5), 15, "ship capacity");
        // the library versions return -(insertionPoint) - 1 when the key is missing
        eq(Arrays.binarySearch(a, 4), -3, "Arrays.binarySearch miss");
        eq(-Arrays.binarySearch(a, 4) - 1, 2, "insertion point");
        eq(Collections.binarySearch(List.of(1, 3, 5), 5), 2, "Collections.binarySearch");
        done();
    }
}
