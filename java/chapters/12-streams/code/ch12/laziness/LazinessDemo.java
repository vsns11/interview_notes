package ch12.laziness;

import java.util.*;
import java.util.stream.*;

/** Streams are lazy and process elements one at a time ("vertically"). */
public class LazinessDemo {
    public static void main(String[] args) {
        List<String> names = List.of("amy", "bob", "carl", "dave", "eve");

        // 1. No terminal operation -> nothing runs at all
        Stream<String> pipeline = names.stream().filter(n -> { System.out.println("filter " + n); return true; });
        System.out.println("pipeline built, nothing printed yet");

        // 2. With a terminal op: each element flows through ALL stages before the next element starts
        System.out.println("--- vertical processing + short-circuit (findFirst)");
        Optional<String> first = names.stream()
                .filter(n -> { System.out.println("filter " + n); return n.length() > 3; })
                .map(n -> { System.out.println("map    " + n); return n.toUpperCase(); })
                .findFirst();
        System.out.println("result " + first.orElseThrow() + "  (dave and eve were never touched)");

        // 3. limit short-circuits even an infinite stream
        System.out.println("--- limit on an infinite stream");
        List<Integer> squares = Stream.iterate(1, i -> i + 1)
                .peek(i -> System.out.print("gen " + i + " | "))
                .map(i -> i * i)
                .filter(sq -> sq % 2 == 1)
                .limit(3)
                .toList();
        System.out.println("\nodd squares " + squares);

        // 4. sorted() is a stateful BARRIER: it must see every element before emitting any
        System.out.println("--- sorted() is a barrier");
        Stream.of("c", "a", "b")
                .peek(x -> System.out.println("before sort " + x))
                .sorted()
                .forEach(x -> System.out.println("after sort  " + x));

        // 5. Order of operations matters for cost: filter before map/sort
        int[] mapCalls = {0};
        long c = IntStream.range(0, 1_000).boxed().map(i -> { mapCalls[0]++; return i * 2; }).filter(i -> i < 10).count();
        int[] mapCalls2 = {0};
        long c2 = IntStream.range(0, 1_000).boxed().filter(i -> i < 5).map(i -> { mapCalls2[0]++; return i * 2; }).count();
        System.out.println("map-then-filter: " + mapCalls[0] + " map calls; filter-then-map: " + mapCalls2[0] + " map calls (same count " + c + "=" + c2 + ")");
        // (the int[] counters are for demonstration only -- see 'side effects' in section 12.10)
    }
}
