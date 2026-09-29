package ch09.wildcards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Wildcards {

    // Producer: we only READ numbers from the list -> ? extends
    static double total(Collection<? extends Number> source) {
        double t = 0;
        for (Number n : source) t += n.doubleValue();
        // source.add(1);                   // compile error: can't add to ? extends (except null)
        return t;
    }

    // Consumer: we only WRITE integers into the list -> ? super
    static void fillWithSquares(List<? super Integer> sink, int n) {
        for (int i = 1; i <= n; i++) sink.add(i * i);
        Object o = sink.get(0);                 // reading gives only Object
    }

    // Both: copy from a producer into a consumer (the shape of Collections.copy)
    static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) dest.add(item);
    }

    // Unbounded: works for any list; we only use Object methods and size
    static int countNulls(List<?> list) {
        int c = 0;
        for (Object o : list) if (o == null) c++;
        return c;
    }

    // Wildcard capture: a private generic helper can "name" the unknown type
    static void swapFirstLast(List<?> list) { swapHelper(list); }
    private static <T> void swapHelper(List<T> list) {
        T first = list.get(0);
        list.set(0, list.get(list.size() - 1));
        list.set(list.size() - 1, first);
    }

    public static void main(String[] args) {
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> doubles = List.of(0.5, 0.25);
        System.out.println("total(ints)=" + total(ints) + " total(doubles)=" + total(doubles));

        // Invariance: List<Integer> is NOT a List<Number>
        // List<Number> nums = ints;             // compile error: incompatible types
        List<? extends Number> view = ints;      // OK: read-only view as Numbers
        System.out.println("view.get(0).intValue()=" + view.get(0).intValue());

        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        fillWithSquares(numbers, 3);
        fillWithSquares(objects, 2);
        System.out.println("numbers=" + numbers + " objects=" + objects);

        List<Object> dest = new ArrayList<>(List.of("start"));
        copy(dest, ints);                        // T = Integer: dest is super, src is extends
        System.out.println("copy -> " + dest);

        List<String> withNulls = new ArrayList<>();
        withNulls.add("a"); withNulls.add(null); withNulls.add(null);
        System.out.println("countNulls=" + countNulls(withNulls));

        List<String> letters = new ArrayList<>(List.of("x", "y", "z"));
        swapFirstLast(letters);
        System.out.println("swapFirstLast=" + letters);

        // Arrays are covariant (checked at run time); generics are invariant (checked at compile time)
        Object[] arr = new String[1];
        try {
            arr[0] = 42;
        } catch (ArrayStoreException e) {
            System.out.println("arrays: ArrayStoreException " + e.getMessage());
        }
    }
}
