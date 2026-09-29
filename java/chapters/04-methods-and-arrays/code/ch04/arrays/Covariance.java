package ch04.arrays;

import java.util.Arrays;

public class Covariance {

    static int[] minMax(int[] values) {              // return an array to hand back two results
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int v : values) { min = Math.min(min, v); max = Math.max(max, v); }
        return new int[] {min, max};
    }

    public static void main(String[] args) {
        String[] strings = {"a", "b"};
        Object[] objects = strings;                  // allowed: arrays are covariant
        try {
            objects[0] = 42;                         // compiles, but the real array holds Strings
        } catch (ArrayStoreException e) {
            System.out.println("ArrayStoreException: " + e.getMessage());
        }
        int[] mm = minMax(new int[] {4, -2, 17, 9});
        System.out.println("min=" + mm[0] + " max=" + mm[1]);
        System.out.println(Arrays.toString(new int[0]) + " is an empty array, never return null for 'no results'");
    }
}
