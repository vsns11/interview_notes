package ch10.maps;

import java.util.*;

/**
 * A teaching-size hash map (separate chaining) that mirrors java.util.HashMap's main ideas:
 * hash spreading, power-of-two table, index = hash & (n-1), load factor 0.75, doubling resize.
 * Not a replacement for HashMap -- no treeification, no null keys, not thread-safe.
 */
public class TinyHashMap<K, V> {
    private record Node<K, V>(int hash, K key, V value, Node<K, V> next) {}

    private Node<K, V>[] table;
    private int size;
    private final float loadFactor = 0.75f;

    @SuppressWarnings({"unchecked", "rawtypes"})
    TinyHashMap(int capacity) { table = (Node<K, V>[]) new Node[capacity]; }

    /** Same idea as HashMap.hash(): fold the high 16 bits into the low 16 bits. */
    static int spread(Object key) { int h = key.hashCode(); return h ^ (h >>> 16); }

    void put(K key, V value) {
        int h = spread(key), i = h & (table.length - 1);        // index = hash mod n (n is a power of two)
        for (Node<K, V> n = table[i]; n != null; n = n.next()) {
            if (n.hash() == h && n.key().equals(key)) {          // key present -> rebuild the chain with new value
                table[i] = replace(table[i], key, value);
                return;
            }
        }
        table[i] = new Node<>(h, key, value, table[i]);          // (HashMap appends at the tail; we prepend for brevity)
        if (++size > table.length * loadFactor) resize();
    }

    V get(K key) {
        int h = spread(key);
        for (Node<K, V> n = table[h & (table.length - 1)]; n != null; n = n.next())
            if (n.hash() == h && n.key().equals(key)) return n.value();
        return null;
    }

    private Node<K, V> replace(Node<K, V> head, K key, V value) {
        if (head == null) return null;
        if (head.key().equals(key)) return new Node<>(head.hash(), key, value, head.next());
        return new Node<>(head.hash(), head.key(), head.value(), replace(head.next(), key, value));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void resize() {
        Node<K, V>[] old = table;
        table = (Node<K, V>[]) new Node[old.length * 2];
        System.out.printf("  resize: size=%d exceeded threshold %d -> capacity %d -> %d%n",
                size, (int) (old.length * loadFactor), old.length, table.length);
        for (Node<K, V> head : old)
            for (Node<K, V> n = head; n != null; n = n.next()) {
                int i = n.hash() & (table.length - 1);            // each node stays at i or moves to i + oldCapacity
                table[i] = new Node<>(n.hash(), n.key(), n.value(), table[i]);
            }
    }

    void dump() {
        for (int i = 0; i < table.length; i++) {
            StringBuilder sb = new StringBuilder();
            for (Node<K, V> n = table[i]; n != null; n = n.next()) sb.append(n.key()).append(" -> ");
            if (!sb.isEmpty()) System.out.printf("  bucket %2d: %snull%n", i, sb);
        }
    }

    public static void main(String[] args) {
        TinyHashMap<String, Integer> m = new TinyHashMap<>(4);   // tiny capacity so we can watch it grow
        String[] keys = {"apple", "banana", "cherry", "date", "elder", "fig", "grape"};
        for (String k : keys) {
            System.out.printf("put %-7s hashCode=%11d spread=%11d index(n=%d)=%d%n",
                    k, k.hashCode(), spread(k), m.table.length, spread(k) & (m.table.length - 1));
            m.put(k, k.length());
        }
        System.out.println("final table (capacity " + m.table.length + "):");
        m.dump();
        m.put("fig", 99);
        System.out.println("get(fig)=" + m.get("fig") + " get(kiwi)=" + m.get("kiwi") + " size=" + m.size);
    }
}
