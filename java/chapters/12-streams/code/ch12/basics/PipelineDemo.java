package ch12.basics;

import java.util.*;
import java.util.stream.*;

/** A stream pipeline vs a loop, and the common ways to create streams. */
public class PipelineDemo {
    record Order(String customer, String product, int qty, double price) {
        double total() { return qty * price; }
    }

    static final List<Order> ORDERS = List.of(
            new Order("ana", "laptop", 1, 1200), new Order("bo", "mouse", 3, 25),
            new Order("ana", "monitor", 2, 300), new Order("cy", "laptop", 1, 1150),
            new Order("bo", "desk", 1, 450));

    public static void main(String[] args) {
        // Imperative: HOW to do it
        List<String> loop = new ArrayList<>();
        for (Order o : ORDERS) if (o.total() >= 450) loop.add(o.customer().toUpperCase());
        Collections.sort(loop);
        System.out.println("loop:   " + loop);

        // Declarative: WHAT we want. source -> intermediate ops -> terminal op
        List<String> stream = ORDERS.stream()                  // source
                .filter(o -> o.total() >= 450)                 // intermediate
                .map(o -> o.customer().toUpperCase())          // intermediate
                .sorted()                                      // intermediate
                .toList();                                     // terminal (Java 16)
        System.out.println("stream: " + stream);

        // Creating streams
        System.out.println("Stream.of        " + Stream.of("a", "b", "c").toList());
        System.out.println("Arrays.stream    " + Arrays.stream(new int[]{3, 1, 2}).boxed().toList());
        System.out.println("IntStream.range  " + IntStream.range(0, 5).boxed().toList() + " rangeClosed " + IntStream.rangeClosed(1, 3).boxed().toList());
        System.out.println("iterate(seed,f)  " + Stream.iterate(1, x -> x * 2).limit(8).toList());
        System.out.println("iterate(s,has,f) " + Stream.iterate(1, x -> x < 100, x -> x * 3).toList());   // Java 9
        System.out.println("generate         " + Stream.generate(() -> "ab").limit(3).collect(Collectors.joining("-")));
        System.out.println("String.chars     " + "hey".chars().mapToObj(c -> (char) c).toList());
        System.out.println("Map.entrySet     " + new TreeMap<>(Map.of("x", 1, "y", 2)).entrySet().stream().map(e -> e.getKey() + e.getValue()).toList());
        System.out.println("ofNullable(null) " + Stream.ofNullable(null).count());                     // Java 9
        System.out.println("concat           " + Stream.concat(Stream.of(1, 2), Stream.of(3)).toList());
        System.out.println("lines()          " + "l1\nl2\nl3".lines().count() + " lines");            // Java 11
        Stream.Builder<String> b = Stream.builder(); b.add("built").add("stream");
        System.out.println("builder          " + b.build().toList());
        // Files.lines(path) and BufferedReader.lines() stream a file lazily -- Chapter 13.

        // A stream can be consumed only once
        Stream<String> s = Stream.of("once");
        s.forEach(x -> {});
        try { s.count(); } catch (IllegalStateException e) { System.out.println("reuse -> " + e.getMessage()); }

        // Streams don't modify their source
        List<Integer> src = List.of(3, 1, 2);
        List<Integer> sorted = src.stream().sorted().toList();
        System.out.println("source " + src + " unchanged, result " + sorted);
    }
}
