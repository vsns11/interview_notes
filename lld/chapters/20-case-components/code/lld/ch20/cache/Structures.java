package lld.ch20.cache;

/** One cache entry. It carries its own links, so it can be unlinked in O(1). */
final class Node<K, V> {
    final K key;
    V value;
    long expiresAtMillis;  // 0 = never expires
    int frequency;         // used by LFU
    Node<K, V> prev, next;

    Node(K key, V value, long expiresAtMillis) {
        this.key = key;
        this.value = value;
        this.expiresAtMillis = expiresAtMillis;
    }

    boolean isExpired(long now) { return expiresAtMillis != 0 && now >= expiresAtMillis; }
}

/** Doubly linked list with two sentinel nodes: no null checks at the ends. Newest at the front. */
final class DoublyLinkedList<K, V> {
    private final Node<K, V> head = new Node<>(null, null, 0); // sentinel before the front
    private final Node<K, V> tail = new Node<>(null, null, 0); // sentinel after the back
    private int size;

    DoublyLinkedList() {
        head.next = tail;
        tail.prev = head;
    }

    void addFirst(Node<K, V> n) {
        n.prev = head;
        n.next = head.next;
        head.next.prev = n;
        head.next = n;
        size++;
    }

    void unlink(Node<K, V> n) {
        n.prev.next = n.next;
        n.next.prev = n.prev;
        n.prev = n.next = null;
        size--;
    }

    void moveToFront(Node<K, V> n) {
        unlink(n);
        addFirst(n);
    }

    /** The oldest node (at the back), or null if empty. */
    Node<K, V> last() { return size == 0 ? null : tail.prev; }

    boolean isEmpty() { return size == 0; }
}
