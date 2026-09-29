package ch12.parallel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.*;

/** Parallel streams: when they help, when they hurt, and how to keep them correct.
 *  Timings are MACHINE-DEPENDENT (cores, JIT, load); look at ratios. */
public class ParallelDemo {
    static double time(Runnable r) { r.run(); long t = System.nanoTime(); r.run(); return (System.nanoTime() - t) / 1e6; }
    static long sink;

    static boolean isPrime(long n) {
        if (n < 2) return false;
        for (long d = 2; d * d <= n; d++) if (n % d == 0) return false;
        return true;
    }

    public static void main(String[] args) {
        System.out.println("cores available: " + Runtime.getRuntime().availableProcessors()
                + " (common pool parallelism " + java.util.concurrent.ForkJoinPool.commonPool().getParallelism() + ")");

        // 1. CPU-heavy, independent work on a splittable source: parallel helps
        double seqPrimes = time(() -> sink += LongStream.rangeClosed(1, 3_000_000).filter(ParallelDemo::isPrime).count());
        double parPrimes = time(() -> sink += LongStream.rangeClosed(1, 3_000_000).parallel().filter(ParallelDemo::isPrime).count());
        System.out.printf("count primes <= 3M      sequential %6.1f ms | parallel %6.1f ms%n", seqPrimes, parPrimes);

        // 2. Tiny work per element: overhead dominates
        double seqSmall = time(() -> sink += IntStream.range(0, 1_000).map(i -> i * 2).sum());
        double parSmall = time(() -> sink += IntStream.range(0, 1_000).parallel().map(i -> i * 2).sum());
        System.out.printf("sum 1k doubled          sequential %6.2f ms | parallel %6.2f ms%n", seqSmall, parSmall);

        // 3. Poorly splittable source: Stream.iterate is inherently sequential
        double seqIter = time(() -> sink += Stream.iterate(1L, i -> i + 1).limit(2_000_000).reduce(0L, Long::sum));
        double parIter = time(() -> sink += Stream.iterate(1L, i -> i + 1).limit(2_000_000).parallel().reduce(0L, Long::sum));
        System.out.printf("iterate+limit 2M sum    sequential %6.1f ms | parallel %6.1f ms%n", seqIter, parIter);

        // Correctness: results must not depend on shared mutable state
        List<Integer> safe = IntStream.range(0, 10_000).parallel().boxed().toList();          // collector handles merging
        Map<Integer, Long> counts = IntStream.range(0, 10_000).parallel().boxed()
                .collect(Collectors.groupingByConcurrent(i -> i % 3, ConcurrentHashMap::new, Collectors.counting()));
        System.out.println("safe toList size=" + safe.size() + ", in encounter order? " + safe.equals(IntStream.range(0, 10_000).boxed().toList())
                + ", groupingByConcurrent=" + new TreeMap<>(counts));

        // reduce needs an identity that really is neutral, and an associative function
        int good = IntStream.rangeClosed(1, 100).parallel().reduce(0, Integer::sum);
        int bad  = IntStream.rangeClosed(1, 100).parallel().reduce(5, Integer::sum);          // 5 is NOT an identity for +
        System.out.println("reduce identity 0 -> " + good + ", identity 5 -> " + bad + " (5 added once per chunk; the exact value depends on how the work was split)");

        // findAny vs findFirst, forEach vs forEachOrdered
        System.out.print("forEach (parallel, order not guaranteed):   ");
        IntStream.range(0, 8).parallel().forEach(i -> System.out.print(i + " "));
        System.out.print("\nforEachOrdered (parallel, encounter order): ");
        IntStream.range(0, 8).parallel().forEachOrdered(i -> System.out.print(i + " "));
        System.out.println();
        System.out.println("(sink " + (sink != 0) + ")");
    }
}
