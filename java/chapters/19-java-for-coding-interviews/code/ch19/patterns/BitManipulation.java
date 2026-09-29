package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Bit manipulation: single number (XOR), counting bits (DP), swap odd/even bits, power of two, subsets by mask. */
public class BitManipulation {
    /** Every value appears twice except one. x ^ x == 0 and x ^ 0 == x, so the pairs cancel. */
    static int singleNumber(int[] a) {
        int x = 0;
        for (int v : a) x ^= v;
        return x;
    }

    /** bits[i] = number of 1-bits in i, for 0..n. i >> 1 drops the last bit; (i & 1) is that bit. O(n). */
    static int[] countBits(int n) {
        int[] bits = new int[n + 1];
        for (int i = 1; i <= n; i++) bits[i] = bits[i >> 1] + (i & 1);
        return bits;
    }

    /** Swap every even-position bit with its odd neighbour. >>> so the sign bit does not smear in. */
    static int swapOddEvenBits(int x) {
        int even = x & 0x55555555;                 // bits 0, 2, 4, ... (0101...)
        int odd  = x & 0xAAAAAAAA;                 // bits 1, 3, 5, ... (1010...)
        return (even << 1) | (odd >>> 1);
    }

    /** A power of two has exactly one 1-bit; n & (n - 1) clears the lowest 1-bit. */
    static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    /** All subsets: mask bit i set means "take nums[i]". 2^n masks. */
    static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << nums.length); mask++) {
            List<Integer> s = new ArrayList<>();
            for (int i = 0; i < nums.length; i++) if ((mask >> i & 1) == 1) s.add(nums[i]);
            out.add(s);
        }
        return out;
    }

    public static void main(String[] args) {
        eq(singleNumber(new int[]{4, 1, 2, 1, 2}), 4, "singleNumber");
        eq(singleNumber(new int[]{-7, 3, 3}), -7, "singleNumber negative");
        eq(countBits(5), new int[]{0, 1, 1, 2, 1, 2}, "countBits 0..5");
        eq(countBits(5)[5], Integer.bitCount(5), "matches Integer.bitCount");
        eq(swapOddEvenBits(0b10111000), 0b01110100, "swap odd/even bits of 184");
        eq(swapOddEvenBits(Integer.MIN_VALUE), 1 << 30, "sign bit moves right, no smear");
        eq(isPowerOfTwo(64), true, "64 is a power of two");
        eq(isPowerOfTwo(0), false, "0 is not");
        eq(isPowerOfTwo(Integer.MIN_VALUE), false, "MIN_VALUE is not (negative)");
        eq(subsets(new int[]{1, 2, 3}).size(), 8, "2^3 subsets");
        eq(subsets(new int[]{1, 2}), List.of(List.of(), List.of(1), List.of(2), List.of(1, 2)), "subsets by mask");
        done();
    }
}
