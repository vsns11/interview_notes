package ch11.functional;

import java.util.*;
import java.util.function.*;

/** The java.util.function package: the core shapes and their primitive specialisations. */
public class BuiltInInterfaces {
    record Order(String id, double amount, boolean express) {}

    public static void main(String[] args) {
        Supplier<List<String>> newList = ArrayList::new;          // () -> T
        Consumer<String> log = s -> System.out.println("log: " + s); // T -> void
        BiConsumer<String, Integer> put = (k, v) -> System.out.println(k + "=" + v);
        Predicate<Order> big = o -> o.amount() > 100;              // T -> boolean
        BiPredicate<String, Integer> longerThan = (s, n) -> s.length() > n;
        Function<Order, String> toId = Order::id;                  // T -> R
        BiFunction<Double, Double, Double> pow = Math::pow;        // (T, U) -> R
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";  // T -> T
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Comparator.naturalOrder()); // (T, T) -> T

        List<String> l = newList.get(); l.add("x");
        log.accept("supplier made " + l);
        put.accept("retries", 3);
        Order o = new Order("A-1", 250.0, true);
        System.out.println("big? " + big.test(o) + ", longerThan(\"java\",3)? " + longerThan.test("java", 3)
                + ", id=" + toId.apply(o) + ", 2^10=" + pow.apply(2.0, 10.0) + ", " + shout.apply("hey") + ", max=" + max.apply(4, 9));

        // Primitive specialisations avoid boxing: IntPredicate, IntFunction, ToIntFunction, IntUnaryOperator, ...
        IntPredicate even = n -> n % 2 == 0;
        IntUnaryOperator square = n -> n * n;
        IntBinaryOperator add = Integer::sum;
        ToIntFunction<String> len = String::length;
        IntFunction<String> stars = "*"::repeat;
        IntSupplier dice = () -> 4;                 // chosen by fair dice roll
        ToDoubleBiFunction<Integer, Integer> ratio = (a, b) -> (double) a / b;
        ObjIntConsumer<String> repeatPrint = (s, n) -> System.out.println(s.repeat(n));
        System.out.println("even(6)=" + even.test(6) + " square(7)=" + square.applyAsInt(7) + " add=" + add.applyAsInt(2, 3)
                + " len=" + len.applyAsInt("hello") + " stars=" + stars.apply(3) + " dice=" + dice.getAsInt() + " ratio=" + ratio.applyAsDouble(1, 4));
        repeatPrint.accept("ab", 3);

        // Why the primitive versions exist: boxing every value costs time and memory
        int n = 20_000_000;
        Function<Integer, Integer> boxedInc = x -> x + 1;
        IntUnaryOperator primInc = x -> x + 1;
        long t0 = System.nanoTime(); Integer b = 0; for (int i = 0; i < n; i++) b = boxedInc.apply(b);
        long t1 = System.nanoTime(); int p = 0;     for (int i = 0; i < n; i++) p = primInc.applyAsInt(p);
        long t2 = System.nanoTime();
        System.out.printf("20M calls: Function<Integer,Integer> %.0f ms vs IntUnaryOperator %.0f ms (machine-dependent) results equal=%b%n",
                (t1 - t0) / 1e6, (t2 - t1) / 1e6, b == p);
    }
}
