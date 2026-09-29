package ch11.lambdas;

import java.util.*;
import java.util.function.*;

/** From anonymous class to lambda; syntax forms; target typing; a custom functional interface. */
public class LambdaBasics {
    /** A functional interface = exactly one abstract method. The annotation makes the compiler check that. */
    @FunctionalInterface
    interface Discount {
        double apply(double price);                          // the single abstract method
        default Discount then(Discount next) { return p -> next.apply(apply(p)); }  // defaults are allowed
    }

    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("Charlie", "al", "Bea"));

        // Before Java 8: an anonymous class just to pass one method
        names.sort(new Comparator<String>() {
            @Override public int compare(String a, String b) { return Integer.compare(a.length(), b.length()); }
        });
        System.out.println("anonymous class: " + names);

        // Java 8: the same behaviour as a lambda
        names.sort((a, b) -> Integer.compare(b.length(), a.length()));   // longest first
        System.out.println("lambda:          " + names);

        // Syntax forms
        Runnable noArgs          = () -> System.out.println("no parameters");
        Function<String, Integer> oneArg = s -> s.length();                 // parentheses optional for one untyped param
        BinaryOperator<Integer> typed    = (Integer a, Integer b) -> a + b;  // explicit types
        BiFunction<Integer, Integer, Integer> withVar = (var a, var b) -> a * b; // Java 11: var params
        Function<Integer, String> block  = n -> {                           // block body needs return
            if (n < 0) return "negative";
            return "non-negative";
        };
        noArgs.run();
        System.out.println(oneArg.apply("lambda") + " " + typed.apply(2, 3) + " " + withVar.apply(4, 5) + " " + block.apply(-1));

        // Target typing: the SAME lambda text becomes different interfaces depending on context
        Predicate<String> isEmpty1 = s -> s.isEmpty();
        Function<String, Boolean> isEmpty2 = s -> s.isEmpty();
        System.out.println("Predicate.test=" + isEmpty1.test("") + " Function.apply=" + isEmpty2.apply("x"));

        // A lambda needs a target type -- 'var f = x -> x;' does not compile.
        // Custom functional interface, with a default method for composition
        Discount tenOff = p -> p - 10;
        Discount twentyPercent = p -> p * 0.8;
        System.out.println("100 -> tenOff then 20% = " + tenOff.then(twentyPercent).apply(100));

        // Lambdas are objects: they can be stored, passed, and returned
        Map<String, BinaryOperator<Integer>> ops = Map.of("+", Integer::sum, "*", (a, b) -> a * b, "max", Math::max);
        for (String op : List.of("+", "*", "max")) System.out.print(op + "=" + ops.get(op).apply(6, 7) + " ");
        System.out.println();
    }
}
