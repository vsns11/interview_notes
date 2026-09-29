package ch04.arrays;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ArraysUtil {
    public static void main(String[] args) {
        int[] nums = {42, 7, 19, 3, 25};
        Arrays.sort(nums);
        System.out.println("sort:          " + Arrays.toString(nums));
        System.out.println("binarySearch(19) = " + Arrays.binarySearch(nums, 19)
                + ", binarySearch(20) = " + Arrays.binarySearch(nums, 20));  // -(insertion point) - 1

        String[] words = {"pear", "Fig", "apple", "kiwi"};
        Arrays.sort(words);                                   // natural order: uppercase before lowercase
        System.out.println("sort strings:  " + Arrays.toString(words));
        Arrays.sort(words, String.CASE_INSENSITIVE_ORDER);
        System.out.println("ignore case:   " + Arrays.toString(words));
        Arrays.sort(words, Comparator.comparing(String::length).thenComparing(Comparator.reverseOrder()));
        System.out.println("by length:     " + Arrays.toString(words));

        int[] filled = new int[5];
        Arrays.fill(filled, -1);
        System.out.println("fill:          " + Arrays.toString(filled));
        int[] squares = new int[6];
        Arrays.setAll(squares, i -> i * i);
        System.out.println("setAll:        " + Arrays.toString(squares));

        System.out.println("copyOf(3):     " + Arrays.toString(Arrays.copyOf(nums, 3)));
        System.out.println("copyOf(7):     " + Arrays.toString(Arrays.copyOf(nums, 7)));   // pads with 0
        System.out.println("copyOfRange:   " + Arrays.toString(Arrays.copyOfRange(nums, 1, 4)));
        int[] dest = new int[7];
        System.arraycopy(nums, 0, dest, 2, nums.length);
        System.out.println("arraycopy:     " + Arrays.toString(dest));

        int[] x = {1, 2, 3}, y = {1, 2, 4};
        System.out.println("equals=" + Arrays.equals(x, y) + " compare=" + Arrays.compare(x, y)
                + " mismatch=" + Arrays.mismatch(x, y) + " hashCode equal? "
                + (Arrays.hashCode(x) == Arrays.hashCode(new int[] {1, 2, 3})));

        System.out.println("stream sum:    " + Arrays.stream(nums).sum()
                + ", max: " + Arrays.stream(nums).max().getAsInt());

        String[] backing = {"a", "b", "c"};
        List<String> view = Arrays.asList(backing);           // fixed-size list backed by the array
        view.set(0, "z");
        System.out.println("asList view:   " + view + " array now " + Arrays.toString(backing));
        try {
            view.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("asList add ->  UnsupportedOperationException");
        }
        List<String> independent = List.of(backing);          // copy, unmodifiable
        backing[1] = "changed";
        System.out.println("List.of copy:  " + independent);
    }
}
