package lld.ch13.bloomfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * A thread-safe Bloom filter: a compact set that answers "definitely NOT present" or
 * "PROBABLY present". No false negatives; a tunable rate of false positives.
 * Lock-free: bits live in an AtomicLongArray and are set with an atomic OR.
 */
public class Main {

    static final class ConcurrentBloomFilter {
        private final AtomicLongArray words;     // m bits packed into 64-bit words
        private final int numBits;               // m
        private final int numHashes;             // k

        /** Size the filter for 'expectedItems' at the target false-positive probability. */
        ConcurrentBloomFilter(int expectedItems, double falsePositiveRate) {
            double ln2 = Math.log(2);
            long m = (long) Math.ceil(-expectedItems * Math.log(falsePositiveRate) / (ln2 * ln2));
            if (m > Integer.MAX_VALUE - 63) throw new IllegalArgumentException("too large");
            this.numBits = (int) m;
            this.numHashes = Math.max(1, (int) Math.round((double) m / expectedItems * ln2));
            this.words = new AtomicLongArray((numBits + 63) / 64);
        }

        /** Adds the item. Returns true if at least one bit changed (i.e. it was definitely new). */
        boolean add(String item) {
            long base = fnv1a64(item);
            long h1 = fmix64(base), h2 = fmix64(base ^ 0x9E3779B97F4A7C15L) | 1;   // h2 odd
            boolean changed = false;
            for (int i = 0; i < numHashes; i++) {
                int bit = (int) Math.floorMod(h1 + i * h2, (long) numBits);        // double hashing
                long mask = 1L << (bit & 63);
                long before = words.getAndAccumulate(bit >>> 6, mask, (cur, m) -> cur | m);  // atomic OR (CAS loop)
                if ((before & mask) == 0) changed = true;
            }
            return changed;
        }

        boolean mightContain(String item) {
            long base = fnv1a64(item);
            long h1 = fmix64(base), h2 = fmix64(base ^ 0x9E3779B97F4A7C15L) | 1;
            for (int i = 0; i < numHashes; i++) {
                int bit = (int) Math.floorMod(h1 + i * h2, (long) numBits);
                if ((words.get(bit >>> 6) & (1L << (bit & 63))) == 0) return false;   // definitely absent
            }
            return true;
        }

        int numBits() { return numBits; }
        int numHashes() { return numHashes; }

        // 64-bit FNV-1a over the string's chars, then a strong bit-mixer (MurmurHash3's finalizer).
        private static long fnv1a64(String s) {
            long h = 0xcbf29ce484222325L;
            for (int i = 0; i < s.length(); i++) { h ^= s.charAt(i); h *= 0x100000001b3L; }
            return h;
        }
        private static long fmix64(long k) {
            k ^= k >>> 33; k *= 0xff51afd7ed558ccdL;
            k ^= k >>> 33; k *= 0xc4ceb9fe1a85ec53L;
            k ^= k >>> 33;
            return k;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int THREADS = 8, PER_THREAD = 10_000, N = THREADS * PER_THREAD;
        ConcurrentBloomFilter bf = new ConcurrentBloomFilter(N, 0.01);
        System.out.println("sized for n=" + N + ", p=1%: m=" + bf.numBits() + " bits (" + bf.numBits() / 8 / 1024
                + " KiB), k=" + bf.numHashes() + " hashes");

        List<Thread> ts = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            final int id = t;
            Thread th = new Thread(() -> { for (int i = 0; i < PER_THREAD; i++) bf.add("user-" + id + "-" + i); });
            ts.add(th); th.start();
        }
        for (Thread th : ts) th.join();

        int falseNegatives = 0;
        for (int t = 0; t < THREADS; t++) for (int i = 0; i < PER_THREAD; i++) if (!bf.mightContain("user-" + t + "-" + i)) falseNegatives++;
        System.out.println("false negatives after 8-thread insert: " + falseNegatives);

        int probes = 100_000, falsePositives = 0;
        for (int i = 0; i < probes; i++) if (bf.mightContain("stranger-" + i)) falsePositives++;
        System.out.printf("false-positive rate on %,d unseen keys: %.2f%% (target 1%%)%n", probes, 100.0 * falsePositives / probes);
        System.out.println("add(\"user-0-0\") again changes bits? " + bf.add("user-0-0"));
    }
}
