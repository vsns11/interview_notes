package lld.ch13.stripedmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

/**
 * A simplified concurrent hash map using LOCK STRIPING: the buckets are guarded by a small array
 * of locks, so threads touching different stripes never block each other.
 * (Java 7's ConcurrentHashMap used this idea with "segments".)
 */
public class Main {

    static final class StripedHashMap<K, V> {
        private static final class Node<K, V> {
            final K key; final int hash; V value; Node<K, V> next;
            Node(K key, int hash, V value, Node<K, V> next) { this.key = key; this.hash = hash; this.value = value; this.next = next; }
        }

        private volatile Node<K, V>[] table;                // replaced (never mutated in place) only while ALL locks are held
        private final ReentrantLock[] stripes;
        private final AtomicInteger size = new AtomicInteger();
        private static final float LOAD_FACTOR = 0.75f;

        @SuppressWarnings({"unchecked", "rawtypes"})
        StripedHashMap(int initialBuckets, int stripeCount) {
            if (Integer.bitCount(stripeCount) != 1 || Integer.bitCount(initialBuckets) != 1)
                throw new IllegalArgumentException("sizes must be powers of two");
            if (stripeCount > initialBuckets)                 // guarantees: every bucket maps to exactly one stripe
                throw new IllegalArgumentException("need stripes <= buckets");
            table = (Node<K, V>[]) new Node[initialBuckets];
            stripes = new ReentrantLock[stripeCount];
            for (int i = 0; i < stripeCount; i++) stripes[i] = new ReentrantLock();
        }

        private static int spread(Object key) {
            int h = key.hashCode();
            return (h ^ (h >>> 16)) & 0x7fffffff;             // mix high bits in, keep it non-negative
        }
        // A key's stripe depends only on its hash, NOT on the table size, so it never changes on resize.
        private ReentrantLock stripeFor(int hash) { return stripes[hash & (stripes.length - 1)]; }
        private static int indexFor(int hash, int len) { return hash & (len - 1); }

        V get(K key) {
            Objects.requireNonNull(key);
            int h = spread(key);
            ReentrantLock lock = stripeFor(h);
            lock.lock();
            try {
                Node<K, V>[] t = table;                           // stable while we hold a stripe lock
                for (Node<K, V> n = t[indexFor(h, t.length)]; n != null; n = n.next)
                    if (n.hash == h && n.key.equals(key)) return n.value;
                return null;
            } finally { lock.unlock(); }
        }

        /** Returns the previous value (or null). */
        V put(K key, V value) {
            Objects.requireNonNull(value);
            return doCompute(key, (k, old) -> value).oldValue();
        }

        V remove(K key) {
            Objects.requireNonNull(key);
            int h = spread(key);
            ReentrantLock lock = stripeFor(h);
            lock.lock();
            try {
                Node<K, V>[] t = table;
                int i = indexFor(h, t.length);
                for (Node<K, V> prev = null, n = t[i]; n != null; prev = n, n = n.next) {
                    if (n.hash == h && n.key.equals(key)) {
                        if (prev == null) t[i] = n.next; else prev.next = n.next;
                        size.decrementAndGet();
                        return n.value;
                    }
                }
                return null;
            } finally { lock.unlock(); }
        }

        /** Atomic read-modify-write for one key; returns the new value. Returning null from remap removes the key. */
        V compute(K key, BiFunction<? super K, ? super V, ? extends V> remap) {
            return doCompute(key, remap).newValue();
        }

        private record Result<V>(V oldValue, V newValue) { }

        private Result<V> doCompute(K key, BiFunction<? super K, ? super V, ? extends V> remap) {
            Objects.requireNonNull(key);
            int h = spread(key);
            ReentrantLock lock = stripeFor(h);
            boolean grew = false;
            V old, nv;
            lock.lock();
            try {
                Node<K, V>[] t = table;
                int i = indexFor(h, t.length);
                Node<K, V> prev = null, n = t[i];
                while (n != null && !(n.hash == h && n.key.equals(key))) { prev = n; n = n.next; }
                old = n == null ? null : n.value;
                nv = remap.apply(key, old);
                if (n != null && nv != null) {
                    n.value = nv;                                   // update in place
                } else if (n != null) {                             // remap said "remove"
                    if (prev == null) t[i] = n.next; else prev.next = n.next;
                    size.decrementAndGet();
                } else if (nv != null) {                            // insert at bucket head
                    t[i] = new Node<>(key, h, nv, t[i]);
                    grew = size.incrementAndGet() > t.length * LOAD_FACTOR;
                }
            } finally { lock.unlock(); }
            if (grew) resize();                                     // never resize while holding one stripe
            return new Result<>(old, nv);
        }

        /** Stop-the-world resize: take every stripe lock IN ORDER (no deadlock), rehash, publish. */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private void resize() {
            for (ReentrantLock l : stripes) l.lock();
            try {
                Node<K, V>[] old = table;
                if (size.get() <= old.length * LOAD_FACTOR) return;  // another thread already resized
                Node<K, V>[] nt = (Node<K, V>[]) new Node[old.length * 2];
                for (Node<K, V> head : old) {
                    for (Node<K, V> n = head; n != null; ) {
                        Node<K, V> next = n.next;
                        int j = indexFor(n.hash, nt.length);
                        n.next = nt[j]; nt[j] = n;
                        n = next;
                    }
                }
                table = nt;
            } finally {
                for (int i = stripes.length - 1; i >= 0; i--) stripes[i].unlock();
            }
        }

        int size() { return size.get(); }
        int capacity() { return table.length; }
    }

    static void runThreads(int n, java.util.function.IntConsumer body) throws InterruptedException {
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < n; i++) { final int id = i; Thread t = new Thread(() -> body.accept(id)); ts.add(t); t.start(); }
        for (Thread t : ts) t.join();
    }

    public static void main(String[] args) throws InterruptedException {
        StripedHashMap<String, Integer> map = new StripedHashMap<>(16, 16);
        map.put("a", 1);
        System.out.println("put a=1, put a=2 returns old=" + map.put("a", 2) + ", get a=" + map.get("a")
                + ", remove a=" + map.remove("a") + ", get a=" + map.get("a"));

        final int THREADS = 8, PER = 20_000;
        runThreads(THREADS, id -> { for (int i = 0; i < PER; i++) map.put("t" + id + "-" + i, i); });
        int missing = 0;
        for (int id = 0; id < THREADS; id++) for (int i = 0; i < PER; i++) if (!Integer.valueOf(i).equals(map.get("t" + id + "-" + i))) missing++;
        System.out.println("8 threads x 20,000 puts -> size=" + map.size() + ", missing=" + missing
                + ", table grew from 16 to " + map.capacity() + " buckets");

        StripedHashMap<String, Integer> counters = new StripedHashMap<>(16, 8);
        runThreads(THREADS, id -> {
            for (int i = 0; i < 10_000; i++) counters.compute("hot-" + (i % 10), (k, v) -> v == null ? 1 : v + 1);
        });
        int total = 0;
        for (int i = 0; i < 10; i++) total += counters.get("hot-" + i);
        System.out.println("atomic compute on 10 hot keys: each=" + counters.get("hot-0") + ", total=" + total + " (expected 80000)");

        runThreads(THREADS, id -> { for (int i = 0; i < PER; i++) map.remove("t" + id + "-" + i); });
        System.out.println("after concurrent removes size=" + map.size());
    }
}
