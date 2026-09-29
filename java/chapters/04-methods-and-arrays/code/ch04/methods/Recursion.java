package ch04.methods;

import java.util.HashMap;
import java.util.Map;

public class Recursion {

    static long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        if (n <= 1) return 1;                 // base case: stops the recursion
        return n * factorial(n - 1);          // recursive case: smaller problem
    }

    static long fibNaive(int n) {             // O(2^n): recomputes the same values again and again
        return n < 2 ? n : fibNaive(n - 1) + fibNaive(n - 2);
    }

    static final Map<Integer, Long> memo = new HashMap<>();
    static long fibMemo(int n) {              // O(n): each value computed once
        if (n < 2) return n;
        Long cached = memo.get(n);
        if (cached != null) return cached;
        long value = fibMemo(n - 1) + fibMemo(n - 2);
        memo.put(n, value);
        return value;
    }

    static long fibLoop(int n) {              // O(n) time, O(1) space, no stack risk
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) { long t = a + b; a = b; b = t; }
        return a;
    }

    static int binarySearch(int[] a, int key, int lo, int hi) {
        if (lo > hi) return -1;
        int mid = lo + (hi - lo) / 2;         // avoids int overflow of (lo + hi)
        if (a[mid] == key) return mid;
        return a[mid] < key ? binarySearch(a, key, mid + 1, hi)
                            : binarySearch(a, key, lo, mid - 1);
    }

    static int depth = 0;
    static void dive() { depth++; dive(); }   // no base case

    public static void main(String[] args) {
        System.out.println("5! = " + factorial(5) + ", 20! = " + factorial(20));
        System.out.println("fibNaive(30) = " + fibNaive(30));
        System.out.println("fibMemo(90)  = " + fibMemo(90));
        System.out.println("fibLoop(90)  = " + fibLoop(90));
        int[] sorted = {2, 5, 8, 12, 16, 23, 38, 56};
        System.out.println("index of 23 = " + binarySearch(sorted, 23, 0, sorted.length - 1));
        try {
            dive();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError after roughly " + (depth / 1000) * 1000 + "+ calls");
        }
    }
}
