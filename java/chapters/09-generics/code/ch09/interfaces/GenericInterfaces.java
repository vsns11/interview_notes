package ch09.interfaces;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GenericInterfaces {

    // Our own generic functional interface
    @FunctionalInterface
    interface Transformer<A, B> {
        B apply(A input);
        default <C> Transformer<A, C> andThen(Transformer<? super B, ? extends C> next) {
            return a -> next.apply(apply(a));
        }
    }

    // Implementing Comparable<T> with a concrete type argument
    record Version(int major, int minor) implements Comparable<Version> {
        public int compareTo(Version o) {
            int c = Integer.compare(major, o.major);
            return c != 0 ? c : Integer.compare(minor, o.minor);
        }
        @Override public String toString() { return major + "." + minor; }
    }

    // A generic interface implemented three ways
    interface Container<T> { void put(T item); T take(); boolean isEmpty(); }

    static class StringStack implements Container<String> {           // 1. concrete type argument
        private final List<String> items = new ArrayList<>();
        public void put(String s) { items.add(s); }
        public String take() { return items.removeLast(); }           // removeLast: Java 21
        public boolean isEmpty() { return items.isEmpty(); }
    }

    static class Queue<T> implements Container<T> {                   // 2. stays generic
        private final java.util.ArrayDeque<T> items = new java.util.ArrayDeque<>();
        public void put(T t) { items.addLast(t); }
        public T take() { return items.pollFirst(); }
        public boolean isEmpty() { return items.isEmpty(); }
    }

    static <T> List<T> drain(Container<T> c) {
        List<T> out = new ArrayList<>();
        while (!c.isEmpty()) out.add(c.take());
        return out;
    }

    public static void main(String[] args) {
        Transformer<String, Integer> length = String::length;
        Transformer<String, String> describe = length.andThen(n -> n + " chars");
        System.out.println(describe.apply("generics"));

        List<Version> versions = new ArrayList<>(List.of(new Version(21, 0), new Version(17, 2), new Version(21, 1), new Version(8, 0)));
        versions.sort(null);                                                  // natural order via Comparable
        System.out.println("natural order: " + versions);
        versions.sort(Comparator.comparingInt(Version::minor).thenComparing(Comparator.reverseOrder()));
        System.out.println("by minor, then descending: " + versions);

        Container<String> stack = new StringStack();
        stack.put("a"); stack.put("b"); stack.put("c");
        Container<Integer> queue = new Queue<>();
        queue.put(1); queue.put(2); queue.put(3);
        Container<Runnable> tasks = new Container<>() {                       // 3. anonymous class with diamond (Java 9)
            private Runnable r;
            public void put(Runnable x) { r = x; }
            public Runnable take() { Runnable t = r; r = null; return t; }
            public boolean isEmpty() { return r == null; }
        };
        tasks.put(() -> System.out.println("task ran"));
        System.out.println("stack drains LIFO: " + drain(stack) + ", queue drains FIFO: " + drain(queue));
        drain(tasks).forEach(Runnable::run);
    }
}
