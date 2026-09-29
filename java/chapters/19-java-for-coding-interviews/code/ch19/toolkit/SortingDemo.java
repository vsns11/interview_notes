package ch19.toolkit;

import java.util.*;

/** Sorting primitives, objects, 2D arrays and lists with comparators. */
public class SortingDemo {
    record Person(String name, int age) {}

    public static void main(String[] args) {
        // primitives: ascending only (no comparator overload for int[])
        int[] nums = {3, -1, 2};
        Arrays.sort(nums);
        System.out.println("int[] asc        " + Arrays.toString(nums));

        // descending ints: box to Integer[] (or sort ascending and read backwards)
        Integer[] boxed = {3, -1, 2};
        Arrays.sort(boxed, Collections.reverseOrder());
        System.out.println("Integer[] desc   " + Arrays.toString(boxed));
        int[] desc = Arrays.stream(nums).boxed().sorted(Comparator.reverseOrder()).mapToInt(Integer::intValue).toArray();
        System.out.println("stream desc      " + Arrays.toString(desc));

        // 2D arrays such as intervals: sort by start, then by end
        int[][] intervals = {{5, 6}, {1, 9}, {1, 3}};
        Arrays.sort(intervals, Comparator.<int[]>comparingInt(x -> x[0]).thenComparingInt(x -> x[1]));
        System.out.println("intervals        " + Arrays.deepToString(intervals));

        // lists: List.sort and Collections.sort are the same stable merge sort (TimSort)
        List<Person> people = new ArrayList<>(List.of(
                new Person("Ravi", 30), new Person("Ana", 25), new Person("Bo", 30)));
        people.sort(Comparator.comparingInt(Person::age).reversed().thenComparing(Person::name));
        System.out.println("age desc, name   " + people);
        List<String> words = new ArrayList<>(List.of("pear", "fig", "apple", "kiwi"));
        Collections.sort(words, Comparator.comparingInt(String::length));  // stable: pear before kiwi
        System.out.println("by length        " + words);

        // the subtraction comparator bug: a - b overflows for far-apart values
        Integer[] big = {Integer.MIN_VALUE, 1, Integer.MAX_VALUE};
        Arrays.sort(big, (x, y) -> x - y);                    // WRONG
        System.out.println("(a-b) comparator " + Arrays.toString(big));
        Arrays.sort(big, Integer::compare);                   // right
        System.out.println("Integer::compare " + Arrays.toString(big));
    }
}
