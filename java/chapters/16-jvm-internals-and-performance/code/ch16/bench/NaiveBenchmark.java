package ch16.bench;

/**
 * Why hand-written timing loops lie: (1) warm-up — the same code gets faster as the JIT kicks in;
 * (2) dead-code elimination — work whose result is unused can be deleted by the JIT.
 * Try also: java -Xint ... (interpreter only) and java -XX:TieredStopAtLevel=1 ... (C1 only).
 */
public class NaiveBenchmark {

    static long collatzSteps(long n) {                      // some real CPU work
        long steps = 0;
        while (n != 1) { n = (n % 2 == 0) ? n / 2 : 3 * n + 1; steps++; }
        return steps;
    }

    static long work(int from, int to) {
        long total = 0;
        for (int i = from; i < to; i++) total += collatzSteps(i);
        return total;
    }

    static double sink;                                     // consuming results defeats dead-code elimination

    public static void main(String[] args) {
        System.out.println("1) Warm-up: identical rounds of 20,000 Collatz sequences");
        for (int round = 1; round <= 8; round++) {
            long t0 = System.nanoTime();
            long r = work(1, 20_001);
            System.out.printf("   round %d: %6.2f ms (result %d)%n", round, (System.nanoTime() - t0) / 1e6, r);
        }

        System.out.println("2) Dead code: 50M square roots, result ignored vs used");
        for (int round = 1; round <= 3; round++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < 50_000_000; i++) Math.sqrt(i);          // result discarded
            long ignored = System.nanoTime() - t0;

            t0 = System.nanoTime();
            double acc = 0;
            for (int i = 0; i < 50_000_000; i++) acc += Math.sqrt(i);   // result used
            sink = acc;
            long used = System.nanoTime() - t0;
            System.out.printf("   round %d: ignored %7.2f ms   used %7.2f ms%n", round, ignored / 1e6, used / 1e6);
        }
    }
}
