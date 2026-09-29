package ch11.composition;

import java.util.*;
import java.util.function.*;

/** Combining small functions into bigger ones with the default/static methods of the functional interfaces. */
public class CompositionDemo {
    public static void main(String[] args) {
        // Function: andThen (this first, then next) vs compose (next first, then this)
        Function<Integer, Integer> plus2 = x -> x + 2;
        Function<Integer, Integer> times3 = x -> x * 3;
        System.out.println("plus2.andThen(times3)(5) = " + plus2.andThen(times3).apply(5)   // (5+2)*3
                + ", plus2.compose(times3)(5) = " + plus2.compose(times3).apply(5));          // 5*3+2
        System.out.println("identity = " + Function.<String>identity().apply("same"));

        // A text-cleaning pipeline built from reusable steps
        UnaryOperator<String> trim = String::strip;
        UnaryOperator<String> lower = String::toLowerCase;
        UnaryOperator<String> collapse = s -> s.replaceAll("\\s+", " ");
        Function<String, String> clean = trim.andThen(lower).andThen(collapse);
        System.out.println("clean -> [" + clean.apply("   Hello    WORLD  ") + "]");

        // Predicate: and, or, negate, not (Java 11), isEqual
        Predicate<String> nonBlank = Predicate.not(String::isBlank);
        Predicate<String> shortWord = s -> s.length() <= 4;
        Predicate<String> startsJ = s -> s.startsWith("J");
        List<String> words = List.of("Java", "", "Jakarta", "Go", "  ", "JVM");
        System.out.println("nonBlank&short: " + filter(words, nonBlank.and(shortWord))
                + "  startsJ|short: " + filter(words, startsJ.or(shortWord))
                + "  !startsJ: " + filter(words, startsJ.negate().and(nonBlank))
                + "  isEqual(Go): " + filter(words, Predicate.isEqual("Go")));

        // Consumer.andThen: run several side effects in order
        Consumer<String> audit = s -> System.out.print("[audit " + s + "] ");
        Consumer<String> send = s -> System.out.print("[send " + s + "]");
        audit.andThen(send).accept("msg-1");
        System.out.println();

        // BiFunction.andThen, BinaryOperator.minBy/maxBy
        BiFunction<Integer, Integer, Integer> area = (w, h) -> w * h;
        System.out.println("area.andThen(format) = " + area.andThen(a -> a + " m2").apply(3, 4)
                + ", minBy length = " + BinaryOperator.minBy(Comparator.comparingInt(String::length)).apply("kiwi", "fig"));

        // Higher-order function: returns a function (a 'function factory')
        Function<Double, UnaryOperator<Double>> taxAdder = rate -> price -> price * (1 + rate);
        UnaryOperator<Double> vat20 = taxAdder.apply(0.20);
        System.out.println("vat20(50.0) = " + vat20.apply(50.0));

        // Composition is also how Comparators are built (see Chapter 10)
        Comparator<String> cmp = Comparator.comparingInt(String::length).thenComparing(Comparator.reverseOrder());
        List<String> sorted = new ArrayList<>(List.of("bb", "a", "cc", "ddd", "aa"));
        sorted.sort(cmp);
        System.out.println("length, then reverse alpha: " + sorted);
    }

    static List<String> filter(List<String> in, Predicate<String> p) {
        List<String> out = new ArrayList<>();
        for (String s : in) if (p.test(s)) out.add(s);
        return out;
    }
}
