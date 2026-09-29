package ch10.utilities;

import java.util.*;

/** The java.util.Collections toolbox. */
public class CollectionsUtilDemo {
    public static void main(String[] args) {
        List<Integer> xs = new ArrayList<>(List.of(5, 3, 8, 1, 9, 3));
        System.out.println("max=" + Collections.max(xs) + " min=" + Collections.min(xs)
                + " frequency(3)=" + Collections.frequency(xs, 3));

        Collections.sort(xs);
        System.out.println("sorted=" + xs + " binarySearch(8)=" + Collections.binarySearch(xs, 8)
                + " binarySearch(4)=" + Collections.binarySearch(xs, 4) + " (negative: -(insertionPoint)-1)");

        Collections.reverse(xs);           System.out.println("reverse=" + xs);
        Collections.swap(xs, 0, xs.size() - 1); System.out.println("swap ends=" + xs);
        Collections.rotate(xs, 2);         System.out.println("rotate 2=" + xs);
        Collections.shuffle(xs, new Random(42)); System.out.println("shuffle(seed 42)=" + xs);

        System.out.println("nCopies=" + Collections.nCopies(3, "ab") + " disjoint=" + Collections.disjoint(List.of(1, 2), List.of(3)));
        List<String> letters = new ArrayList<>(List.of("c", "a"));
        Collections.addAll(letters, "d", "b");
        System.out.println("addAll varargs=" + letters);

        // Unmodifiable VIEW vs unmodifiable COPY
        List<String> source = new ArrayList<>(List.of("x"));
        List<String> view = Collections.unmodifiableList(source);
        List<String> copy = List.copyOf(source);
        source.add("y");
        System.out.println("view sees change: " + view + "  copy does not: " + copy);
        try { view.add("z"); } catch (UnsupportedOperationException e) { System.out.println("view.add -> UnsupportedOperationException"); }

        // Empty and singleton helpers (immutable)
        System.out.println("emptyList=" + Collections.emptyList() + " singleton=" + Collections.singletonList("only"));

        // synchronizedList: every method locks, but iteration still needs a manual lock
        List<Integer> sync = Collections.synchronizedList(new ArrayList<>(List.of(1, 2)));
        synchronized (sync) { for (int v : sync) System.out.print("sync " + v + " "); }
        System.out.println();
    }
}
