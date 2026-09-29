package ch10.sets;

import java.util.*;

/** HashSet vs LinkedHashSet vs TreeSet, plus TreeSet navigation. */
public class SetDemo {
    public static void main(String[] args) {
        List<String> visits = List.of("home", "cart", "home", "search", "cart", "about");
        System.out.println("HashSet       " + new HashSet<>(visits) + "  (order not guaranteed)");
        System.out.println("LinkedHashSet " + new LinkedHashSet<>(visits) + "  (insertion order)");
        System.out.println("TreeSet       " + new TreeSet<>(visits) + "  (sorted)");

        // Set algebra with bulk operations
        Set<String> a = new TreeSet<>(Set.of("java", "sql", "git"));
        Set<String> b = Set.of("git", "docker");
        Set<String> union = new TreeSet<>(a); union.addAll(b);
        Set<String> inter = new TreeSet<>(a); inter.retainAll(b);
        Set<String> diff = new TreeSet<>(a); diff.removeAll(b);
        System.out.println("union=" + union + " intersection=" + inter + " difference=" + diff);

        // TreeSet is a NavigableSet: nearest-match queries in O(log n)
        TreeSet<Integer> prices = new TreeSet<>(List.of(5, 12, 20, 35, 50));
        System.out.println("floor(19)=" + prices.floor(19) + " ceiling(19)=" + prices.ceiling(19)
                + " lower(20)=" + prices.lower(20) + " higher(20)=" + prices.higher(20));
        System.out.println("headSet(20)=" + prices.headSet(20) + " tailSet(20)=" + prices.tailSet(20)
                + " subSet(10,40)=" + prices.subSet(10, 40));
        System.out.println("first=" + prices.first() + " pollLast=" + prices.pollLast() + " descending=" + prices.descendingSet());

        // A mutable key mutated after insertion is lost inside a HashSet
        List<String> key = new ArrayList<>(List.of("x"));
        Set<List<String>> set = new HashSet<>();
        set.add(key);
        key.add("y");                                // hashCode changes!
        System.out.println("contains mutated key? " + set.contains(key) + ", size=" + set.size());

        // TreeSet rejects null (it must compare); HashSet allows one null
        Set<String> hs = new HashSet<>(); hs.add(null);
        try { new TreeSet<String>().add(null); } catch (NullPointerException e) { System.out.println("HashSet null ok=" + hs.contains(null) + ", TreeSet.add(null) -> NPE"); }
    }
}
