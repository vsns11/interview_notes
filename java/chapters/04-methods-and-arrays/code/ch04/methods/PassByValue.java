package ch04.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PassByValue {

    static void addOne(int x) { x = x + 1; }                 // changes the local copy only

    static void reassign(int[] arr) { arr = new int[] {99}; } // re-points the local copy only

    static void mutate(int[] arr) { arr[0] = 99; }           // follows the copied reference to the SAME array

    static void appendTo(List<String> list) { list.add("added"); }

    static void swap(String a, String b) { String t = a; a = b; b = t; }  // cannot work in Java

    public static void main(String[] args) {
        int n = 5;
        addOne(n);
        System.out.println("n after addOne:      " + n);

        int[] data = {1, 2, 3};
        reassign(data);
        System.out.println("data after reassign: " + Arrays.toString(data));
        mutate(data);
        System.out.println("data after mutate:   " + Arrays.toString(data));

        List<String> names = new ArrayList<>(List.of("a"));
        appendTo(names);
        System.out.println("names after append:  " + names);

        String x = "left", y = "right";
        swap(x, y);
        System.out.println("after swap:          " + x + " " + y);
    }
}
