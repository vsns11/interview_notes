package ch11.fp;

import java.io.IOException;
import java.util.*;
import java.util.function.*;

/** Functional-programming ideas in everyday Java: pure functions, higher-order functions,
 *  currying, laziness, memoisation, strategies, and checked exceptions in lambdas. */
public class FunctionalStyle {
    // Impure: depends on and changes outside state
    static int total = 0;
    static int addToTotal(int x) { total += x; return total; }
    // Pure: output depends only on input, no side effects
    static int add(int a, int b) { return a + b; }

    // Higher-order function: takes a function as an argument
    static <T> List<T> retryUntil(Supplier<T> attempt, Predicate<T> ok, int max) {
        List<T> tries = new ArrayList<>();
        for (int i = 0; i < max; i++) { T r = attempt.get(); tries.add(r); if (ok.test(r)) break; }
        return tries;
    }

    // Memoisation: cache results of a pure function
    static <T, R> Function<T, R> memoize(Function<T, R> f) {
        Map<T, R> cache = new HashMap<>();
        return x -> {
            R r = cache.get(x);
            if (r == null) { r = f.apply(x); cache.put(x, r); }   // NOT computeIfAbsent: see note in README
            return r;
        };
    }

    static int slowSquareCalls = 0;
    static int slowSquare(int x) { slowSquareCalls++; return x * x; }

    // Checked exceptions don't fit java.util.function types -> wrap them
    @FunctionalInterface interface ThrowingFunction<T, R> { R apply(T t) throws Exception; }
    static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
        return t -> {
            try { return f.apply(t); }
            catch (RuntimeException e) { throw e; }
            catch (Exception e) { throw new RuntimeException(e); }
        };
    }
    static String readConfig(String name) throws IOException {
        if (name.startsWith("missing")) throw new IOException("no such file: " + name);
        return name + "=ok";
    }

    public static void main(String[] args) {
        System.out.println("impure twice: " + addToTotal(5) + ", " + addToTotal(5) + "   pure twice: " + add(2, 3) + ", " + add(2, 3));

        Iterator<Integer> rolls = List.of(2, 5, 6, 3).iterator();
        System.out.println("retryUntil six: " + retryUntil(rolls::next, r -> r == 6, 10));

        // Currying: a function of two args as a chain of one-arg functions
        Function<Integer, Function<Integer, Integer>> curriedAdd = a -> b -> a + b;
        Function<Integer, Integer> add10 = curriedAdd.apply(10);   // partial application
        System.out.println("add10(5)=" + add10.apply(5));

        // Laziness with Supplier: the expensive message is built only if needed
        boolean debug = false;
        logIf(debug, () -> "state dump " + "x".repeat(1_000_000).length());
        System.out.println("lazy log: supplier not called because debug=false");

        Function<Integer, Integer> fastSquare = memoize(FunctionalStyle::slowSquare);
        for (int x : new int[]{4, 4, 5, 4, 5}) fastSquare.apply(x);
        System.out.println("memoized: 5 calls, underlying function ran " + slowSquareCalls + " times");

        // Strategy pattern without classes: a map of lambdas
        Map<String, DoubleUnaryOperator> shipping = Map.of(
                "standard", w -> 4.99,
                "express", w -> 9.99 + 1.5 * w,
                "free", w -> 0.0);
        for (String k : List.of("standard", "express", "free"))
            System.out.printf("%s(2kg)=%.2f  ", k, shipping.get(k).applyAsDouble(2));
        System.out.println();

        Function<String, String> safeRead = unchecked(FunctionalStyle::readConfig);
        System.out.println(safeRead.apply("app.properties"));
        try { safeRead.apply("missing.properties"); }
        catch (RuntimeException e) { System.out.println("wrapped: " + e.getCause()); }

        // Immutability + pure transformation instead of in-place mutation
        List<Integer> prices = List.of(10, 20, 30);
        List<Integer> discounted = prices.stream().map(p -> p * 9 / 10).toList();
        System.out.println("original " + prices + " untouched; new " + discounted);
    }

    static void logIf(boolean enabled, Supplier<String> message) { if (enabled) System.out.println(message.get()); }
}
