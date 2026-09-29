package lld.ch20.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

enum RemovalReason { EVICTED, EXPIRED, REMOVED, CLEARED }

/** Observer: told every time an entry leaves, and why. */
interface EvictionListener<K, V> {
    void onRemoval(K key, V value, RemovalReason reason);
}

/** Hit / miss / eviction / expiry counters. LongAdder is cheap under contention. */
final class CacheStats {
    final LongAdder hits = new LongAdder(), misses = new LongAdder(),
            evictions = new LongAdder(), expirations = new LongAdder();

    @Override
    public String toString() {
        long h = hits.sum(), m = misses.sum();
        return "hits=" + h + " misses=" + m + " evictions=" + evictions.sum() + " expirations=" + expirations.sum()
                + String.format(" hitRate=%.0f%%", h + m == 0 ? 0.0 : 100.0 * h / (h + m));
    }
}

/**
 * Fixed-capacity cache: HashMap for "where is it?", an EvictionPolicy for "who leaves?".
 * Thread-safe with one lock (a read in LRU also writes: it moves the node).
 */
final class Cache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> map = new HashMap<>();
    private final EvictionPolicy<K, V> policy;
    private final LongSupplier clockMillis;
    private final CacheStats stats = new CacheStats();
    private final List<EvictionListener<K, V>> listeners = new CopyOnWriteArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    private record Removal<K, V>(K key, V value, RemovalReason reason) {}

    private Cache(Builder<K, V> b) {
        this.capacity = b.capacity;
        this.policy = EvictionPolicies.create(b.policyType);
        this.clockMillis = b.clockMillis;
    }

    static <K, V> Builder<K, V> builder() { return new Builder<>(); }

    Optional<V> get(K key) {
        List<Removal<K, V>> removed = new ArrayList<>(1);
        lock.lock();
        try {
            Node<K, V> n = map.get(key);
            if (n == null) {
                stats.misses.increment();
                return Optional.empty();
            }
            if (n.isExpired(clockMillis.getAsLong())) { // a miss, an expiry and a removal at once
                stats.misses.increment();
                removed.add(remove(n, RemovalReason.EXPIRED));
                return Optional.empty();
            }
            stats.hits.increment();
            policy.onAccess(n);
            return Optional.of(n.value);
        } finally {
            lock.unlock();
            notifyListeners(removed); // outside the lock: a slow listener cannot block the cache
        }
    }

    void put(K key, V value) { put(key, value, null); }

    void put(K key, V value, Duration ttl) {
        List<Removal<K, V>> removed = new ArrayList<>(1);
        lock.lock();
        try {
            long expiresAt = ttl == null ? 0 : clockMillis.getAsLong() + ttl.toMillis();
            Node<K, V> existing = map.get(key);
            if (existing != null) {          // replace: counts as a use
                existing.value = value;
                existing.expiresAtMillis = expiresAt;
                policy.onAccess(existing);
                return;
            }
            if (map.size() >= capacity) removed.add(remove(policy.victim(), RemovalReason.EVICTED));
            Node<K, V> n = new Node<>(key, value, expiresAt);
            map.put(key, n);
            policy.onInsert(n);
        } finally {
            lock.unlock();
            notifyListeners(removed);
        }
    }

    boolean invalidate(K key) {
        List<Removal<K, V>> removed = new ArrayList<>(1);
        lock.lock();
        try {
            Node<K, V> n = map.get(key);
            if (n != null) removed.add(remove(n, RemovalReason.REMOVED));
            return n != null;
        } finally {
            lock.unlock();
            notifyListeners(removed);
        }
    }

    /** Called by a background sweeper (e.g. a ScheduledExecutorService) to reclaim memory early. */
    int purgeExpired() {
        List<Removal<K, V>> removed = new ArrayList<>();
        lock.lock();
        try {
            long now = clockMillis.getAsLong();
            for (Node<K, V> n : new ArrayList<>(map.values()))
                if (n.isExpired(now)) removed.add(remove(n, RemovalReason.EXPIRED));
            return removed.size();
        } finally {
            lock.unlock();
            notifyListeners(removed);
        }
    }

    void clear() {
        List<Removal<K, V>> removed = new ArrayList<>();
        lock.lock();
        try {
            for (Node<K, V> n : new ArrayList<>(map.values())) removed.add(remove(n, RemovalReason.CLEARED));
        } finally {
            lock.unlock();
            notifyListeners(removed);
        }
    }

    /** THE single exit path for every entry, whatever the reason. Caller holds the lock. */
    private Removal<K, V> remove(Node<K, V> n, RemovalReason reason) {
        map.remove(n.key);
        policy.onRemove(n);
        if (reason == RemovalReason.EVICTED) stats.evictions.increment();
        if (reason == RemovalReason.EXPIRED) stats.expirations.increment();
        return new Removal<>(n.key, n.value, reason);
    }

    private void notifyListeners(List<Removal<K, V>> removed) {
        for (Removal<K, V> r : removed)
            for (EvictionListener<K, V> l : listeners) l.onRemoval(r.key(), r.value(), r.reason());
    }

    void addListener(EvictionListener<K, V> l) { listeners.add(l); }
    CacheStats stats() { return stats; }

    int size() {
        lock.lock();
        try { return map.size(); } finally { lock.unlock(); }
    }

    /** Builder: optional settings with sensible defaults, validated once. */
    static final class Builder<K, V> {
        private int capacity = 100;
        private PolicyType policyType = PolicyType.LRU;
        private LongSupplier clockMillis = System::currentTimeMillis;

        Builder<K, V> capacity(int c) { this.capacity = c; return this; }
        Builder<K, V> policy(PolicyType p) { this.policyType = p; return this; }
        Builder<K, V> clock(LongSupplier c) { this.clockMillis = c; return this; }

        Cache<K, V> build() {
            if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
            return new Cache<>(this);
        }
    }
}

/** Sharding: N independent caches chosen by key hash, so threads rarely share a lock. */
final class ShardedCache<K, V> {
    private final List<Cache<K, V>> shards = new ArrayList<>();

    ShardedCache(int shardCount, int capacityPerShard, PolicyType type) {
        for (int i = 0; i < shardCount; i++)
            shards.add(Cache.<K, V>builder().capacity(capacityPerShard).policy(type).build());
    }

    private Cache<K, V> shard(K key) { return shards.get(Math.floorMod(key.hashCode(), shards.size())); }
    Optional<V> get(K key) { return shard(key).get(key); }
    void put(K key, V value) { shard(key).put(key, value); }
    int size() { return shards.stream().mapToInt(Cache::size).sum(); }
}
