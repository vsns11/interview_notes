package lld.ch10.iterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** A tree has no index. The iterator hides an in-order walk behind hasNext/next. */
final class BinarySearchTree<T extends Comparable<T>> implements Iterable<T> {
    private static final class Node<T> {
        final T value;
        Node<T> left, right;
        Node(T value) { this.value = value; }
    }

    private Node<T> root;

    void insert(T value) {
        Node<T> fresh = new Node<>(value);
        if (root == null) { root = fresh; return; }
        Node<T> cur = root;
        while (true) {
            if (value.compareTo(cur.value) < 0) {
                if (cur.left == null) { cur.left = fresh; return; }
                cur = cur.left;
            } else {
                if (cur.right == null) { cur.right = fresh; return; }
                cur = cur.right;
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            // The stack holds the path still to visit: this IS the iterator's position.
            private final Deque<Node<T>> stack = new ArrayDeque<>();
            { pushLeft(root); }

            private void pushLeft(Node<T> n) {
                for (; n != null; n = n.left) stack.push(n);
            }

            @Override public boolean hasNext() { return !stack.isEmpty(); }

            @Override
            public T next() {
                if (stack.isEmpty()) throw new NoSuchElementException();
                Node<T> n = stack.pop();
                pushLeft(n.right);
                return n.value;
            }
        };
    }
}
