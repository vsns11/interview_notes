package ch15.random;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

/** One shared java.util.Random (one CAS-updated seed) vs ThreadLocalRandom (a seed per thread). */
public class RandomContention {
    static final Random SHARED = new Random();
    static volatile long sink;

    public static void main(String[] args) throws InterruptedException {
        int threads = Runtime.getRuntime().availableProcessors();
        long shared = time(threads, () -> SHARED.nextInt(100));
        long local = time(threads, () -> ThreadLocalRandom.current().nextInt(100));
        System.out.println(threads + " threads x 5,000,000 nextInt(100)   [machine-dependent]");
        System.out.println("shared Random      : " + shared + " ms");
        System.out.println("ThreadLocalRandom  : " + local + " ms  (" + Math.round((double) shared / local) + "x faster)");
        System.out.println("range helper       : nextInt(10, 20) in [10,20): "
                + inRange(ThreadLocalRandom.current().nextInt(10, 20)));
        try {
            ThreadLocalRandom.current().setSeed(42);
        } catch (UnsupportedOperationException e) {
            System.out.println("setSeed            : UnsupportedOperationException (not reproducible by design)");
        }
    }

    static boolean inRange(int x) { return x >= 10 && x < 20; }

    static long time(int threads, IntSupplier next) throws InterruptedException {
        Thread[] ts = new Thread[threads];
        long t0 = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> { long s = 0; for (int k = 0; k < 5_000_000; k++) s += next.getAsInt(); sink = s; });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        return (System.nanoTime() - t0) / 1_000_000;
    }
}
