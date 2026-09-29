package ch10.maps;

import java.util.*;

/** Everyday Map API: get/put, the Java 8 default methods, iteration, TreeMap navigation, LRU. */
public class MapDemo {
    public static void main(String[] args) {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("apple", 5);
        Integer old = stock.put("apple", 7);                    // replaces, returns previous value
        System.out.println("old=" + old + " get=" + stock.get("apple") + " missing=" + stock.get("kiwi")
                + " default=" + stock.getOrDefault("kiwi", 0));

        stock.putIfAbsent("pear", 3);                           // only if key absent (or mapped to null)
        stock.merge("apple", 10, Integer::sum);                 // 7 + 10
        stock.merge("plum", 1, Integer::sum);                   // absent -> just puts 1
        stock.compute("pear", (k, v) -> v == null ? 1 : v * 2); // recompute from old value
        stock.computeIfPresent("plum", (k, v) -> null);         // returning null REMOVES the key
        System.out.println("stock=" + new TreeMap<>(stock));

        // Word count: the idiom you will write a hundred times
        Map<String, Integer> counts = new TreeMap<>();
        for (String w : "to be or not to be".split(" ")) counts.merge(w, 1, Integer::sum);
        System.out.println("counts=" + counts);

        // Grouping: computeIfAbsent creates the inner list on first use
        Map<Character, List<String>> byInitial = new TreeMap<>();
        for (String n : List.of("ana", "bo", "al", "cy", "ben"))
            byInitial.computeIfAbsent(n.charAt(0), c -> new ArrayList<>()).add(n);
        System.out.println("byInitial=" + byInitial);

        // Iterate entries (not keys + get) -- one lookup per entry
        for (Map.Entry<String, Integer> e : counts.entrySet())
            if (e.getValue() > 1) System.out.print(e.getKey() + "x" + e.getValue() + " ");
        System.out.println();

        // TreeMap: sorted keys + NavigableMap queries (e.g. tax brackets, time series)
        TreeMap<Integer, String> bracket = new TreeMap<>(Map.of(0, "10%", 11_000, "12%", 44_725, "22%", 95_375, "24%"));
        System.out.println("income 50000 -> " + bracket.floorEntry(50_000).getValue()
                + ", first bracket above 50000 starts at " + bracket.higherKey(50_000)
                + ", headMap(44725)=" + bracket.headMap(44_725));

        // LinkedHashMap with accessOrder=true + removeEldestEntry = a tiny LRU cache
        Map<String, String> lru = new LinkedHashMap<>(16, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) { return size() > 3; }
        };
        lru.put("a", "A"); lru.put("b", "B"); lru.put("c", "C");
        lru.get("a");                 // touch a -> most recently used
        lru.put("d", "D");            // evicts b, the least recently used
        System.out.println("LRU keys=" + lru.keySet());

        // HashMap allows one null key; TreeMap (natural order) does not
        Map<String, Integer> hm = new HashMap<>(); hm.put(null, 0);
        System.out.println("HashMap null key -> " + hm.get(null));
    }
}
