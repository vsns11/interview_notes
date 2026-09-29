package ch12.primitive;

import java.util.*;
import java.util.stream.*;

/** IntStream / LongStream / DoubleStream: numeric streams without boxing. */
public class PrimitiveStreams {
    public static void main(String[] args) {
        int[] scores = {72, 95, 88, 61, 79};
        IntStream s = Arrays.stream(scores);
        System.out.println("sum=" + IntStream.of(scores).sum() + " max=" + IntStream.of(scores).max().getAsInt()
                + " average=" + s.average().orElse(0));
        System.out.println("empty average -> " + IntStream.empty().average() + ", empty sum -> " + IntStream.empty().sum());

        IntSummaryStatistics st = IntStream.of(scores).summaryStatistics();
        System.out.println("stats " + st);

        // Converting between object and primitive streams
        List<String> words = List.of("alpha", "be", "gamma");
        int totalChars = words.stream().mapToInt(String::length).sum();          // Stream<T> -> IntStream
        List<Integer> boxed = IntStream.rangeClosed(1, 5).boxed().toList();      // IntStream -> Stream<Integer>
        List<String> labels = IntStream.range(0, 3).mapToObj(i -> "row" + i).toList();
        double[] halves = IntStream.of(1, 2, 3).mapToDouble(i -> i / 2.0).toArray();
        System.out.println("totalChars=" + totalChars + " boxed=" + boxed + " labels=" + labels + " halves=" + Arrays.toString(halves));

        // Index-based loops as streams
        List<String> names = List.of("x", "y", "z");
        System.out.println("indexed: " + IntStream.range(0, names.size()).mapToObj(i -> i + ":" + names.get(i)).collect(Collectors.joining(" ")));

        // Characters: chars() gives an IntStream of UTF-16 code units
        String text = "Hello World";
        long vowels = text.toLowerCase().chars().filter(c -> "aeiou".indexOf(c) >= 0).count();
        System.out.println("vowels in '" + text + "' = " + vowels);

        // Factorial with LongStream and overflow awareness
        System.out.println("20! = " + LongStream.rangeClosed(1, 20).reduce(1, (a, b) -> a * b) + " (21! overflows long)");

        // Random numbers as a stream (seeded -> repeatable)
        System.out.println("dice: " + new Random(7).ints(5, 1, 7).boxed().toList());

        // Boxing cost, measured: timings are MACHINE-DEPENDENT
        int n = 10_000_000;
        long t0 = System.nanoTime();
        long a = IntStream.range(0, n).asLongStream().sum();
        long t1 = System.nanoTime();
        long b = Stream.iterate(0L, i -> i + 1).limit(n).reduce(0L, Long::sum);
        long t2 = System.nanoTime();
        System.out.printf("sum 0..10M: LongStream %.0f ms vs Stream<Long> %.0f ms (same result: %b)%n", (t1 - t0) / 1e6, (t2 - t1) / 1e6, a == b);
    }
}
