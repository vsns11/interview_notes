package ch19.toolkit;

import java.util.*;

/** HashMap / HashSet idioms that replace five lines of if-else with one call. */
public class MapSetIdioms {
    public static void main(String[] args) {
        String[] words = {"to", "be", "or", "not", "to", "be"};

        // 1. counting: merge (cleanest) or getOrDefault
        Map<String, Integer> count = new HashMap<>();
        for (String w : words) count.merge(w, 1, Integer::sum);
        Map<String, Integer> count2 = new HashMap<>();
        for (String w : words) count2.put(w, count2.getOrDefault(w, 0) + 1);
        System.out.println("merge        " + new TreeMap<>(count) + "  same=" + count.equals(count2));

        // 2. grouping: computeIfAbsent returns the (new or existing) list, so you can add straight away
        Map<Integer, List<String>> byLength = new TreeMap<>();
        for (String w : words) byLength.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);
        System.out.println("group        " + byLength);

        // 3. iterate entries (not keySet + get)
        for (Map.Entry<String, Integer> e : new TreeMap<>(count).entrySet()) {
            if (e.getValue() > 1) System.out.print(e.getKey() + "=" + e.getValue() + " ");
        }
        System.out.println();

        // 4. get on a missing key returns null -> unboxing it throws NullPointerException
        try {
            int n = count.get("missing");
            System.out.println(n);
        } catch (NullPointerException ex) {
            System.out.println("unboxing null -> NullPointerException");
        }

        // 5. Set.add returns false for a duplicate: a one-line "seen before?" test
        Set<Integer> seen = new HashSet<>();
        int firstDup = -1;
        for (int x : new int[]{3, 1, 4, 1, 5}) {
            if (!seen.add(x)) { firstDup = x; break; }
        }
        System.out.println("first dup    " + firstDup);

        // 6. keys must have value-based equals/hashCode: arrays do NOT, lists and records DO
        Set<int[]> arrays = new HashSet<>();
        arrays.add(new int[]{1, 2});
        System.out.println("int[] key    contains? " + arrays.contains(new int[]{1, 2}));
        Set<List<Integer>> lists = new HashSet<>();
        lists.add(List.of(1, 2));
        System.out.println("List key     contains? " + lists.contains(List.of(1, 2)));
        record Cell(int r, int c) {}
        Set<Cell> cells = new HashSet<>(Set.of(new Cell(0, 1)));
        System.out.println("record key   contains? " + cells.contains(new Cell(0, 1)));

        // 7. HashMap order is unspecified; LinkedHashMap = insertion order; TreeMap = sorted
        Map<String, Integer> linked = new LinkedHashMap<>();
        for (String w : words) linked.merge(w, 1, Integer::sum);
        System.out.println("linked order " + linked.keySet());
    }
}
