package ch10.perf;

import java.util.*;

/**
 * Rough, single-shot timings that make big-O visible. MACHINE-DEPENDENT and not a rigorous
 * benchmark (use JMH for that -- Chapter 16). Compare the ratios within one row, not absolute numbers.
 */
public class CollectionBenchmark {
    static double ms(Runnable r) { r.run(); long t = System.nanoTime(); r.run(); return (System.nanoTime() - t) / 1e6; }
    static long sink;

    public static void main(String[] args) {
        int n = 100_000;
        List<Integer> array = new ArrayList<>(), linked = new LinkedList<>();
        for (int i = 0; i < n; i++) { array.add(i); linked.add(i); }

        // 1. Random access get(i): O(1) vs O(n)
        Runnable getA = () -> { for (int i = 0; i < 20_000; i++) sink += array.get((i * 7919) % n); };
        Runnable getL = () -> { for (int i = 0; i < 20_000; i++) sink += linked.get((i * 7919) % n); };
        System.out.printf("20k get(i) n=100k        ArrayList %7.1f ms | LinkedList %7.1f ms%n", ms(getA), ms(getL));

        // 2. Insert at the front: ArrayList shifts every element (O(n)); ArrayDeque/LinkedList O(1)
        Runnable frontA = () -> { List<Integer> l = new ArrayList<>(); for (int i = 0; i < 50_000; i++) l.add(0, i); };
        Runnable frontD = () -> { Deque<Integer> d = new ArrayDeque<>(); for (int i = 0; i < 50_000; i++) d.addFirst(i); };
        System.out.printf("50k add-at-front          ArrayList %7.1f ms | ArrayDeque %7.1f ms%n", ms(frontA), ms(frontD));

        // 3. Membership test: contains is O(n) on a list, O(1) average on a HashSet, O(log n) on a TreeSet
        Set<Integer> hash = new HashSet<>(array); Set<Integer> tree = new TreeSet<>(array);
        Runnable cL = () -> { for (int i = 0; i < 2_000; i++) sink += array.contains(n - 1 - i) ? 1 : 0; };
        Runnable cH = () -> { for (int i = 0; i < 2_000; i++) sink += hash.contains(n - 1 - i) ? 1 : 0; };
        Runnable cT = () -> { for (int i = 0; i < 2_000; i++) sink += tree.contains(n - 1 - i) ? 1 : 0; };
        System.out.printf("2k contains n=100k         ArrayList %7.1f ms | HashSet %.1f ms | TreeSet %.1f ms%n", ms(cL), ms(cH), ms(cT));

        // 4. Iterating: both O(n), but ArrayList is contiguous and cache-friendly
        Runnable itA = () -> { for (int k = 0; k < 20; k++) for (int v : array) sink += v; };
        Runnable itL = () -> { for (int k = 0; k < 20; k++) for (int v : linked) sink += v; };
        System.out.printf("20 full iterations        ArrayList %7.1f ms | LinkedList %7.1f ms%n", ms(itA), ms(itL));

        // 5. Pre-sizing: avoids repeated grow-and-copy (ArrayList grows by ~1.5x, HashMap doubles)
        Runnable grow = () -> { List<Integer> l = new ArrayList<>(); for (int i = 0; i < 2_000_000; i++) l.add(i); sink += l.size(); };
        Runnable pre  = () -> { List<Integer> l = new ArrayList<>(2_000_000); for (int i = 0; i < 2_000_000; i++) l.add(i); sink += l.size(); };
        System.out.printf("2M adds                    default %7.1f ms | presized %7.1f ms%n", ms(grow), ms(pre));
        System.out.println("(sink=" + (sink != 0) + ")");
    }
}
