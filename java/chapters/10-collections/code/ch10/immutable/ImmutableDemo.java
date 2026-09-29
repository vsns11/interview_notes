package ch10.immutable;

import java.util.*;
import java.util.stream.Stream;

/** Unmodifiable collections: List.of / Set.of / Map.of (Java 9), copyOf (Java 10), Stream.toList (Java 16). */
public class ImmutableDemo {
    static void attempt(String label, Runnable r) {
        try { r.run(); System.out.println(label + " -> ok"); }
        catch (RuntimeException e) { System.out.println(label + " -> " + e.getClass().getSimpleName()); }
    }

    public static void main(String[] args) {
        List<String> days = List.of("Mon", "Tue", "Wed");
        attempt("List.of(...).add", () -> days.add("Thu"));
        attempt("List.of(...).set", () -> days.set(0, "Sun"));
        attempt("List.of with null", () -> List.of("a", null));
        attempt("List.of(...).contains(null)", () -> days.contains(null));
        attempt("Set.of duplicate", () -> Set.of("a", "a"));
        attempt("Map.of duplicate key", () -> Map.of("k", 1, "k", 2));

        Map<String, Integer> codes = Map.ofEntries(Map.entry("ok", 200), Map.entry("notFound", 404));
        System.out.println("Map.ofEntries get = " + codes.get("notFound"));

        // Arrays.asList: fixed size but set() works; List.of: nothing works
        List<String> fixed = Arrays.asList("a", "b");
        attempt("Arrays.asList set", () -> fixed.set(0, "z"));
        attempt("Arrays.asList add", () -> fixed.add("c"));

        // copyOf takes a snapshot; if the input is already an unmodifiable List.of list it may return it as is
        List<String> src = new ArrayList<>(List.of("a"));
        List<String> snap = List.copyOf(src); src.add("b");
        System.out.println("copyOf snapshot=" + snap + " src=" + src + " copyOf(List.of) same object? " + (List.copyOf(days) == days));

        // Stream.toList() (16) is unmodifiable; Collectors.toList() makes no promise (today an ArrayList)
        List<Integer> t = Stream.of(3, 1, 2).toList();
        attempt("Stream.toList().add", () -> t.add(4));

        // Shallow immutability: the list is fixed, the objects inside are not
        List<StringBuilder> sbs = List.of(new StringBuilder("hi"));
        sbs.get(0).append("!");
        System.out.println("element mutated inside an 'immutable' list: " + sbs);

        // Set.of / Map.of iteration order is unspecified (it is randomized per JVM run) -- never rely on it
        System.out.println("Set.of size=" + Set.of("x", "y", "z").size() + " (print order may differ between runs)");
    }
}
