package ch06.nested;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class NestedDemo {

    // 1. Static nested class: no link to an outer instance. Often used for builders and helpers.
    static class Node {
        final String value;
        Node(String value) { this.value = value; }
    }

    // 2. Inner (non-static member) class: every instance is tied to an outer instance.
    private final List<String> items = new ArrayList<>(List.of("a", "b", "c"));
    private final String owner;
    NestedDemo(String owner) { this.owner = owner; }

    class ReverseIterator implements Iterator<String> {
        private int i = items.size() - 1;                  // reads the OUTER object's field directly
        public boolean hasNext() { return i >= 0; }
        public String next() { return items.get(i--) + "@" + NestedDemo.this.owner; }  // Outer.this
    }

    Supplier<String> counterDescription() {
        int size = items.size();                           // effectively final local variable
        // 3. Local class: declared inside a method, can capture effectively final locals.
        class Describer implements Supplier<String> {
            public String get() { return owner + " has " + size + " items"; }
        }
        return new Describer();
    }

    public static void main(String[] args) {
        Node n = new Node("static nested");                // no outer instance needed
        System.out.println(n.value);

        NestedDemo outer = new NestedDemo("list1");
        NestedDemo.ReverseIterator it = outer.new ReverseIterator();   // needs an outer instance
        while (it.hasNext()) System.out.print(it.next() + " ");
        System.out.println();

        System.out.println(outer.counterDescription().get());

        // 4. Anonymous class: declare and instantiate in one expression.
        Comparator<String> byLengthAnon = new Comparator<>() {
            @Override public int compare(String a, String b) { return Integer.compare(a.length(), b.length()); }
        };
        // Since Java 8, a lambda is shorter for single-method (functional) interfaces:
        Comparator<String> byLengthLambda = (a, b) -> Integer.compare(a.length(), b.length());
        List<String> words = new ArrayList<>(List.of("kiwi", "fig", "banana"));
        words.sort(byLengthAnon);
        System.out.println("anonymous: " + words);
        words.sort(byLengthLambda.reversed());
        System.out.println("lambda reversed: " + words);
    }
}
