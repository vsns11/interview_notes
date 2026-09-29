package ch10.iteration;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** Iterator, ListIterator, fail-fast behaviour and the safe ways to remove while iterating. */
public class IteratorDemo {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

        // 1. Wrong: structural change inside for-each -> fail-fast iterator throws
        try {
            for (Integer n : nums) if (n % 2 == 0) nums.remove(n);
        } catch (ConcurrentModificationException e) {
            System.out.println("for-each + list.remove -> ConcurrentModificationException, list now " + nums);
        }

        // 2. Right: Iterator.remove() removes the element last returned by next()
        nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        for (Iterator<Integer> it = nums.iterator(); it.hasNext(); )
            if (it.next() % 2 == 0) it.remove();
        System.out.println("iterator.remove -> " + nums);

        // 3. Simplest: removeIf (Java 8)
        nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        nums.removeIf(n -> n % 2 == 0);
        System.out.println("removeIf        -> " + nums);

        // ListIterator: both directions, set() and add() during iteration
        List<String> words = new ArrayList<>(List.of("a", "b", "c"));
        for (ListIterator<String> li = words.listIterator(); li.hasNext(); ) {
            String w = li.next();
            li.set(w.toUpperCase());
            if (w.equals("b")) li.add("b2");
        }
        System.out.println("listIterator set/add -> " + words);
        ListIterator<String> back = words.listIterator(words.size());
        StringBuilder rev = new StringBuilder();
        while (back.hasPrevious()) rev.append(back.previous());
        System.out.println("backwards -> " + rev);

        // Map: remove entries via the entrySet view
        Map<String, Integer> scores = new HashMap<>(Map.of("ana", 40, "bo", 90, "cy", 55));
        scores.entrySet().removeIf(e -> e.getValue() < 50);
        System.out.println("passed -> " + new TreeMap<>(scores));

        // Fail-safe (snapshot) iteration: CopyOnWriteArrayList never throws CME
        List<String> listeners = new CopyOnWriteArrayList<>(List.of("L1", "L2"));
        for (String l : listeners) listeners.add(l + "'");   // iterates over the old snapshot
        System.out.println("CopyOnWriteArrayList -> " + listeners);
    }
}
