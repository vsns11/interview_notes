package ch10.sequenced;

import java.util.*;

/** Sequenced collections (Java 21, JEP 431): one API for first/last/reversed. */
public class SequencedDemo {
    public static void main(String[] args) {
        // Before Java 21 each type had its own spelling:
        //   list.get(0), list.get(list.size()-1), deque.getFirst(), sortedSet.first(), linkedHashSet.iterator().next()...
        List<String> list = new ArrayList<>(List.of("b", "c"));
        list.addFirst("a"); list.addLast("d");
        System.out.println("List        first=" + list.getFirst() + " last=" + list.getLast() + " reversed=" + list.reversed());

        LinkedHashSet<String> tags = new LinkedHashSet<>(List.of("java", "sql", "git"));
        tags.addFirst("git");          // already present -> MOVED to the front (Java 21 behaviour)
        System.out.println("LinkedHashSet " + tags + " removeLast=" + tags.removeLast() + " -> " + tags);

        Deque<Integer> dq = new ArrayDeque<>(List.of(1, 2, 3));
        System.out.println("Deque       reversed=" + dq.reversed() + " getLast=" + dq.getLast());

        TreeSet<Integer> sorted = new TreeSet<>(List.of(30, 10, 20));
        System.out.println("TreeSet     first=" + sorted.getFirst() + " last=" + sorted.getLast() + " reversed=" + sorted.reversed());
        try { sorted.addFirst(5); } catch (UnsupportedOperationException e) { System.out.println("TreeSet.addFirst -> UnsupportedOperationException (order comes from the comparator)"); }

        // reversed() is a live VIEW, not a copy
        List<String> rev = list.reversed();
        list.add("e");
        System.out.println("view after list.add(e): " + rev);

        // SequencedMap: LinkedHashMap and TreeMap
        LinkedHashMap<String, Integer> steps = new LinkedHashMap<>();
        steps.put("build", 1); steps.put("test", 2); steps.put("deploy", 3);
        steps.putFirst("checkout", 0);
        System.out.println("SequencedMap first=" + steps.firstEntry() + " last=" + steps.lastEntry()
                + " reversed keys=" + steps.reversed().keySet());
        System.out.println("pollFirstEntry=" + steps.pollFirstEntry() + " -> " + steps.sequencedKeySet());

        // Works generically: one method for any ordered collection
        System.out.println("lastOf(list)=" + lastOf(list) + " lastOf(dq)=" + lastOf(dq) + " lastOf(tags)=" + lastOf(tags));

        try { new ArrayList<String>().getFirst(); } catch (NoSuchElementException e) { System.out.println("empty getFirst -> NoSuchElementException"); }
        System.out.println("HashSet is sequenced? " + (new HashSet<>() instanceof SequencedCollection<?>));
    }

    static <E> E lastOf(SequencedCollection<E> c) { return c.getLast(); }
}
