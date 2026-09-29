package ch04.methods;

import java.util.Arrays;

public class Varargs {

    static int sum(int... numbers) {                 // inside the method, numbers is an int[]
        int total = 0;
        for (int n : numbers) total += n;
        return total;
    }

    // a varargs parameter must be the last one, and there can be only one
    static String join(String separator, String... parts) {
        return String.join(separator, parts);
    }

    static void inspect(Object... items) {
        System.out.println("  length=" + (items == null ? "null array" : items.length)
                + " -> " + Arrays.toString(items));
    }

    public static void main(String[] args) {
        System.out.println(sum());                   // empty array, not null
        System.out.println(sum(4));
        System.out.println(sum(1, 2, 3, 4));
        System.out.println(sum(new int[] {10, 20})); // an existing array is passed as-is
        System.out.println(join(" | ", "a", "b", "c"));
        System.out.println(String.format("%s has %d items", "cart", 3)); // format is a varargs method

        System.out.println("inspect(1, \"two\"):");     inspect(1, "two");
        System.out.println("inspect((Object) null):");  inspect((Object) null); // one null element
        System.out.println("inspect((Object[]) null):"); inspect((Object[]) null); // the array itself is null
    }
}
