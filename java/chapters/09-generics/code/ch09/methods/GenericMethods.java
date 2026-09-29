package ch09.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenericMethods {

    // <T> before the return type declares the method's own type parameter
    static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
    }

    static <T> List<T> repeat(T item, int times) {
        List<T> out = new ArrayList<>();
        for (int i = 0; i < times; i++) out.add(item);
        return out;
    }

    static <T> T firstOrDefault(List<T> list, T fallback) {
        return list.isEmpty() ? fallback : list.get(0);
    }

    public static void main(String[] args) {
        String[] words = {"a", "b", "c"};
        swap(words, 0, 2);                                   // T inferred as String
        Integer[] nums = {1, 2, 3};
        swap(nums, 0, 1);                                    // T inferred as Integer
        System.out.println(Arrays.toString(words) + " " + Arrays.toString(nums));

        List<String> xs = repeat("hi", 3);
        System.out.println("repeat: " + xs);

        // Inference from the target type
        List<String> empty = Collections.emptyList();        // T = String from the assignment
        System.out.println("firstOrDefault: " + firstOrDefault(empty, "none"));

        // Explicit type witness (rarely needed)
        List<Number> mixed = GenericMethods.<Number>repeat(1, 2);
        mixed.add(2.5);
        System.out.println("witness <Number>: " + mixed);

        // Inference picks a common supertype when arguments differ
        var list = Arrays.asList(1, 2.0, 3L);                // List<Number & Comparable<...>>
        System.out.println("Arrays.asList(1, 2.0, 3L) element types: "
                + list.stream().map(o -> o.getClass().getSimpleName()).toList());
    }
}
