package ch12.ops;

import java.util.*;
import java.util.stream.*;

/** Terminal operations: the step that actually runs the pipeline and produces a result. */
public class TerminalOps {
    public static void main(String[] args) {
        List<String> words = List.of("stream", "lambda", "java", "record", "var");

        System.out.println("count            " + words.stream().filter(w -> w.length() > 4).count());
        System.out.println("min / max        " + words.stream().min(Comparator.naturalOrder()).orElseThrow()
                + " / " + words.stream().max(Comparator.comparingInt(String::length)).orElseThrow());
        System.out.println("findFirst        " + words.stream().filter(w -> w.startsWith("r")).findFirst());
        System.out.println("findAny          " + words.stream().filter(w -> w.startsWith("z")).findAny());
        System.out.println("anyMatch         " + words.stream().anyMatch(w -> w.contains("j")));
        System.out.println("allMatch         " + words.stream().allMatch(w -> w.length() >= 3));
        System.out.println("noneMatch        " + words.stream().noneMatch(String::isEmpty));
        System.out.println("allMatch on EMPTY " + Stream.<String>empty().allMatch(w -> false) + " (vacuous truth)");

        // reduce: combine all elements into one
        int sum = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);                 // identity + accumulator
        Optional<Integer> product = Stream.of(1, 2, 3, 4).reduce((a, b) -> a * b); // no identity -> Optional
        int totalLength = words.stream().reduce(0, (acc, w) -> acc + w.length(), Integer::sum); // identity, accumulator, combiner
        System.out.println("reduce           sum=" + sum + " product=" + product.orElse(0) + " totalLength=" + totalLength
                + " emptyReduce=" + Stream.<Integer>empty().reduce(Integer::sum));
        String longest = words.stream().reduce("", (a, b) -> b.length() > a.length() ? b : a);
        System.out.println("reduce longest   " + longest);

        // collect / toList / toArray
        List<String> upper = words.stream().map(String::toUpperCase).toList();            // unmodifiable (16)
        List<String> mutable = words.stream().collect(Collectors.toCollection(ArrayList::new));
        mutable.add("ok");
        String[] arr = words.stream().toArray(String[]::new);
        System.out.println("toList " + upper + " | toCollection+add " + mutable.size() + " | toArray " + arr.length);

        // forEach vs forEachOrdered (they only differ for parallel streams)
        System.out.print("forEachOrdered (parallel): ");
        words.parallelStream().forEachOrdered(w -> System.out.print(w + " "));
        System.out.println();

        // iterator(): pull elements one by one
        Iterator<String> it = words.stream().sorted().iterator();
        System.out.println("iterator first two: " + it.next() + ", " + it.next());
    }
}
