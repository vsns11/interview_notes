package lld.ch20.lrucache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    /** The classic interview answer: HashMap + hand-written doubly linked list. get/put are O(1). */
    static final class LRUCache<K, V> {
        private final class Node {
            K key;
            V value;
            Node prev, next;
            Node(K key, V value) { this.key = key; this.value = value; }
        }

        private final int capacity;
        private final Map<K, Node> index = new HashMap<>();
        private final Node head = new Node(null, null); // sentinel: most recent side
        private final Node tail = new Node(null, null); // sentinel: least recent side

        LRUCache(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
            this.capacity = capacity;
            head.next = tail;
            tail.prev = head;
        }

        synchronized V get(K key) {
            Node n = index.get(key);
            if (n == null) return null;
            unlink(n);
            addFront(n);                 // now the most recently used
            return n.value;
        }

        synchronized void put(K key, V value) {
            Node n = index.get(key);
            if (n != null) {             // update + mark as recently used
                n.value = value;
                unlink(n);
                addFront(n);
                return;
            }
            if (index.size() == capacity) {
                Node lru = tail.prev;    // least recently used lives just before the tail sentinel
                unlink(lru);
                index.remove(lru.key);
            }
            n = new Node(key, value);
            addFront(n);
            index.put(key, n);
        }

        private void addFront(Node n) {
            n.prev = head;
            n.next = head.next;
            head.next.prev = n;
            head.next = n;
        }

        private void unlink(Node n) {
            n.prev.next = n.next;
            n.next.prev = n.prev;
        }

        @Override
        public synchronized String toString() {
            StringBuilder sb = new StringBuilder("[");
            for (Node n = head.next; n != tail; n = n.next) sb.append(n.key).append(n.next == tail ? "" : ", ");
            return sb.append("] (most -> least recent)").toString();
        }
    }

    /** The JDK shortcut: LinkedHashMap in access order + removeEldestEntry. Not thread-safe. */
    static <K, V> Map<K, V> jdkLru(int capacity) {
        return new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) { return size() > capacity; }
        };
    }

    public static void main(String[] args) {
        System.out.println("== Hand-written LRU (LeetCode 146 sequence, capacity 2)");
        LRUCache<Integer, Integer> c = new LRUCache<>(2);
        c.put(1, 1);
        c.put(2, 2);
        System.out.println("get(1) = " + c.get(1) + "   order " + c);
        c.put(3, 3);
        System.out.println("put(3) evicts 2        order " + c);
        System.out.println("get(2) = " + c.get(2));
        c.put(4, 4);
        System.out.println("put(4) evicts 1        order " + c);
        System.out.println("get(1) = " + c.get(1) + ", get(3) = " + c.get(3) + ", get(4) = " + c.get(4));

        System.out.println("== Same sequence with LinkedHashMap(accessOrder=true)");
        Map<Integer, Integer> j = jdkLru(2);
        j.put(1, 1);
        j.put(2, 2);
        j.get(1);
        j.put(3, 3);
        j.put(4, 4);
        System.out.println("keys (least -> most recent) = " + j.keySet());
    }
}
