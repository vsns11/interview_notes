package ch17.v21;

import java.util.*;

/** JEP 431 (Java 21): first/last/reversed on every ordered collection. Full coverage in Chapter 10. */
public class SequencedDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>(List.of("b", "c"));
        list.addFirst("a");
        list.addLast("d");
        System.out.println(list + " first=" + list.getFirst() + " last=" + list.getLast()
                + " reversed=" + list.reversed());

        LinkedHashSet<Integer> set = new LinkedHashSet<>(List.of(3, 1, 2));
        set.addFirst(2);                                             // moves an existing element to the front
        System.out.println(set + " reversed=" + set.reversed());

        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("x", 1); map.put("y", 2); map.putFirst("w", 0);
        System.out.println(map + " firstEntry=" + map.firstEntry() + " pollLast=" + map.pollLastEntry() + " -> " + map);
    }
}
