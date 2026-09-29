package ch04.arrays;

import java.util.Arrays;

public class ArrayBasics {
    public static void main(String[] args) {
        int[] scores = new int[4];                    // fixed size, filled with default values
        String[] names = new String[2];
        boolean[] flags = new boolean[2];
        double[] prices = {9.99, 4.5, 12.0};          // array initializer
        char[] vowels = new char[] {'a', 'e', 'i'};   // explicit form (needed outside a declaration)

        System.out.println("defaults: " + Arrays.toString(scores) + " "
                + Arrays.toString(names) + " " + Arrays.toString(flags));
        scores[0] = 90; scores[3] = 75;
        System.out.println("scores=" + Arrays.toString(scores) + " length=" + scores.length);
        System.out.println("last price = " + prices[prices.length - 1] + ", vowels = " + new String(vowels));

        for (int i = 0; i < prices.length; i++) prices[i] *= 2;   // index loop: can modify
        double total = 0;
        for (double p : prices) total += p;                      // enhanced for: read-only view
        System.out.println("doubled total = " + total);

        try {
            scores[4] = 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("error: " + e.getMessage());
        }

        int[] alias = scores;          // copies the reference: both names point to one array
        int[] copy = scores.clone();   // new array with the same elements
        alias[1] = 50;
        System.out.println("scores=" + Arrays.toString(scores) + " copy=" + Arrays.toString(copy));
        System.out.println("scores == alias? " + (scores == alias) + ", scores == copy? " + (scores == copy)
                + ", Arrays.equals(scores, copy)? " + Arrays.equals(scores, copy));
        System.out.println("println(array) prints an identity string, e.g. starts with: "
                + scores.toString().substring(0, 3));

        var matrix = new int[0];       // var works: the type is int[]
        System.out.println("empty array length = " + matrix.length);
    }
}
