package lld.ch20.cache;

import java.util.HashMap;
import java.util.Map;

/** Strategy: owns the ORDER of entries and answers "who leaves?". The cache owns the map. */
interface EvictionPolicy<K, V> {
    void onInsert(Node<K, V> node);
    void onAccess(Node<K, V> node);
    void onRemove(Node<K, V> node);
    Node<K, V> victim();
}

/** Least recently used: a read moves the entry to the front; the back is the victim. */
final class LruPolicy<K, V> implements EvictionPolicy<K, V> {
    private final DoublyLinkedList<K, V> order = new DoublyLinkedList<>();
    public void onInsert(Node<K, V> n) { order.addFirst(n); }
    public void onAccess(Node<K, V> n) { order.moveToFront(n); }
    public void onRemove(Node<K, V> n) { order.unlink(n); }
    public Node<K, V> victim() { return order.last(); }
}

/** First in, first out: identical to LRU except that a read changes nothing. */
final class FifoPolicy<K, V> implements EvictionPolicy<K, V> {
    private final DoublyLinkedList<K, V> order = new DoublyLinkedList<>();
    public void onInsert(Node<K, V> n) { order.addFirst(n); }
    public void onAccess(Node<K, V> n) { /* insertion order only */ }
    public void onRemove(Node<K, V> n) { order.unlink(n); }
    public Node<K, V> victim() { return order.last(); }
}

/**
 * Least frequently used in O(1): one list per frequency ("bucket"), and the buckets themselves
 * chained in increasing frequency order, so the lowest non-empty bucket is always the first one.
 * Ties inside the lowest bucket go to the least recently used entry (the back of that bucket).
 */
final class LfuPolicy<K, V> implements EvictionPolicy<K, V> {
    private static final class Bucket<K, V> {
        final int frequency;
        final DoublyLinkedList<K, V> entries = new DoublyLinkedList<>();
        Bucket<K, V> lower, higher;           // neighbours in the frequency chain
        Bucket(int frequency) { this.frequency = frequency; }
    }

    private final Map<Integer, Bucket<K, V>> buckets = new HashMap<>();
    private final Bucket<K, V> lowest = new Bucket<>(0);   // sentinels of the bucket chain
    private final Bucket<K, V> highest = new Bucket<>(Integer.MAX_VALUE);

    LfuPolicy() {
        lowest.higher = highest;
        highest.lower = lowest;
    }

    public void onInsert(Node<K, V> n) {
        n.frequency = 1;
        bucketAfter(lowest, 1).entries.addFirst(n);        // frequency 1 is always the lowest
    }

    public void onAccess(Node<K, V> n) {
        Bucket<K, V> from = buckets.get(n.frequency);
        Bucket<K, V> to = bucketAfter(from, n.frequency + 1);
        from.entries.unlink(n);
        dropIfEmpty(from);
        n.frequency++;
        to.entries.addFirst(n);
    }

    public void onRemove(Node<K, V> n) {
        Bucket<K, V> b = buckets.get(n.frequency);
        b.entries.unlink(n);
        dropIfEmpty(b);                                      // the chain stays ordered: nothing to repair
    }

    public Node<K, V> victim() {
        Bucket<K, V> min = lowest.higher;                    // O(1): first bucket in the chain
        return min == highest ? null : min.entries.last();
    }

    /** The bucket for 'frequency', created right after 'prev' if it does not exist yet. */
    private Bucket<K, V> bucketAfter(Bucket<K, V> prev, int frequency) {
        Bucket<K, V> b = buckets.get(frequency);
        if (b != null) return b;
        b = new Bucket<>(frequency);
        b.lower = prev;
        b.higher = prev.higher;
        prev.higher.lower = b;
        prev.higher = b;
        buckets.put(frequency, b);
        return b;
    }

    private void dropIfEmpty(Bucket<K, V> b) {
        if (!b.entries.isEmpty()) return;
        b.lower.higher = b.higher;
        b.higher.lower = b.lower;
        buckets.remove(b.frequency);
    }
}

enum PolicyType { LRU, FIFO, LFU }

/** Factory: maps a configuration value to a policy object. */
final class EvictionPolicies {
    private EvictionPolicies() {}

    static <K, V> EvictionPolicy<K, V> create(PolicyType type) {
        return switch (type) {
            case LRU -> new LruPolicy<>();
            case FIFO -> new FifoPolicy<>();
            case LFU -> new LfuPolicy<>();
        };
    }
}
