package ch11.capture;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

/** Variable capture, effectively final, 'this' in lambdas vs anonymous classes. */
public class CaptureDemo {
    private final String name = "CaptureDemo instance";
    private int counterField = 0;

    void showThis() {
        Runnable lambda = () -> System.out.println("lambda this      -> " + this.name);   // enclosing instance
        Runnable anon = new Runnable() {
            @Override public void run() { System.out.println("anonymous this   -> " + this.getClass().getName()); }
            // 'this' here is the anonymous object itself
        };
        lambda.run(); anon.run();
    }

    void fieldsAreFine() {
        Runnable inc = () -> counterField++;      // fields are NOT subject to effectively-final (captured via 'this')
        inc.run(); inc.run();
        System.out.println("field modified from lambda: " + counterField);
    }

    public static void main(String[] args) {
        int threshold = 10;                        // effectively final: never reassigned
        Predicate<Integer> above = x -> x > threshold;
        System.out.println("above(12)=" + above.test(12));
        // threshold = 11;                         // uncommenting breaks the lambda: "must be final or effectively final"

        // Each lambda captures the VALUE at creation time -- loop variables of enhanced for are fresh each iteration
        List<Supplier<String>> suppliers = new ArrayList<>();
        for (String s : List.of("a", "b", "c")) suppliers.add(() -> s.toUpperCase());
        for (Supplier<String> sup : suppliers) System.out.print(sup.get());
        System.out.println();

        // Classic for-loop index is reassigned, so copy it into a local first
        List<IntSupplier> byIndex = new ArrayList<>();
        for (int i = 0; i < 3; i++) { int copy = i; byIndex.add(() -> copy * 10); }
        byIndex.forEach(f -> System.out.print(f.getAsInt() + " "));
        System.out.println();

        // Need to mutate from inside a lambda? Use a mutable holder -- but prefer a result-returning design
        AtomicInteger count = new AtomicInteger();
        List.of("x", "yy", "zzz").forEach(w -> count.addAndGet(w.length()));
        int better = List.of("x", "yy", "zzz").stream().mapToInt(String::length).sum();
        System.out.println("holder count=" + count.get() + " vs stream sum=" + better);

        CaptureDemo d = new CaptureDemo();
        d.showThis();
        d.fieldsAreFine();

        // Lambdas cannot shadow local variables of the enclosing scope:
        // String s = "x"; Function<String,String> f = s -> s;   // error: variable s is already defined
    }
}
