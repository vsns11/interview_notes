package ch10.maps;

import java.util.*;

/**
 * What happens when every key lands in the same bucket.
 * Timings are MACHINE-DEPENDENT; look at the ratios, not the numbers.
 */
public class HashCollisionDemo {
    /** Terrible hashCode (constant) but Comparable: HashMap can treeify the bucket and search it in O(log n). */
    record CmpKey(int id) implements Comparable<CmpKey> {
        @Override public int hashCode() { return 42; }
        @Override public int compareTo(CmpKey o) { return Integer.compare(id, o.id); }
    }
    /** Same terrible hashCode and NOT Comparable: even the tree cannot order the keys, lookups degrade toward O(n). */
    record PlainKey(int id) {
        @Override public int hashCode() { return 42; }
    }
    /** A good hashCode (the record's default, derived from id). */
    record GoodKey(int id) {}

    static <K> double timeLookups(Map<K, Integer> map, List<K> keys) {
        long t0 = System.nanoTime(); long sum = 0;
        for (int round = 0; round < 3; round++) for (K k : keys) sum += map.get(k);
        double ms = (System.nanoTime() - t0) / 1e6;
        if (sum == 42) System.out.print("");      // keep the JIT from discarding the loop
        return ms;
    }

    static <K> void run(String label, java.util.function.IntFunction<K> make, int n) {
        Map<K, Integer> map = new HashMap<>();
        List<K> keys = new ArrayList<>();
        for (int i = 0; i < n; i++) { K k = make.apply(i); keys.add(k); map.put(k, i); }
        timeLookups(map, keys);                    // warm-up
        System.out.printf("%-34s n=%,d  3n lookups: %8.1f ms%n", label, n, timeLookups(map, keys));
    }

    public static void main(String[] args) {
        int n = 20_000;
        run("good hashCode (GoodKey)", GoodKey::new, n);
        run("constant hash, Comparable (tree)", CmpKey::new, n);
        run("constant hash, not Comparable", PlainKey::new, n);
    }
}
