package ch10.hierarchy;

import java.util.*;

/** The Collection interface: methods every List, Set and Queue shares. */
public class HierarchyDemo {
    public static void main(String[] args) {
        // Program to the interface: the variable type is Collection, the object is an ArrayList.
        Collection<String> langs = new ArrayList<>();
        langs.add("Java");
        langs.add("Go");
        langs.add("Java");                       // Lists allow duplicates
        langs.addAll(List.of("Rust", "Kotlin"));
        System.out.println("list-backed: " + langs + " size=" + langs.size());
        System.out.println("contains Go? " + langs.contains("Go"));
        langs.remove("Java");                    // removes the FIRST match only
        langs.removeIf(s -> s.length() <= 2);    // Java 8: bulk remove by condition
        System.out.println("after removes: " + langs);

        // Swap the implementation, same code: a Set silently drops duplicates.
        Collection<String> unique = new HashSet<>(List.of("Java", "Go", "Rust"));
        System.out.println("add duplicate to set -> " + unique.add("Java"));

        // toArray: typed array from a collection (Java 11 generator form)
        String[] arr = langs.toArray(String[]::new);
        System.out.println("array: " + Arrays.toString(arr) + " isEmpty=" + langs.isEmpty());

        // Maps are NOT Collections, but expose three collection views.
        Map<String, Integer> ages = new TreeMap<>(Map.of("ana", 31, "bo", 25));
        System.out.println("keys=" + ages.keySet() + " values=" + ages.values() + " entries=" + ages.entrySet());
        System.out.println("Map is a Collection? " + (ages instanceof Collection<?>));
    }
}
