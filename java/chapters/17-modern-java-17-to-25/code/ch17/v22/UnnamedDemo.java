package ch17.v22;

import java.util.*;

/** JEP 456 (Java 22): "_" for variables and pattern parts you must declare but never use. */
public class UnnamedDemo {
    record Point(int x, int y) {}
    record Line(Point from, Point to) {}

    public static void main(String[] args) {
        int count = 0;
        for (var _ : List.of("a", "b", "c")) count++;                 // loop variable not needed
        System.out.println("count = " + count);

        try {
            Integer.parseInt("12x");
        } catch (NumberFormatException _) {                          // exception object not needed
            System.out.println("not a number");
        }

        Map<String, Integer> m = new TreeMap<>();
        List.of("x", "y", "x").forEach(k -> m.merge(k, 1, Integer::sum));
        m.forEach((k, _) -> System.out.print(k + " "));               // lambda parameter not needed
        System.out.println();

        Object o = new Line(new Point(0, 0), new Point(3, 4));
        if (o instanceof Line(Point(var x1, _), Point(var x2, _)))    // ignore the y components
            System.out.println("horizontal distance = " + Math.abs(x2 - x1));

        String kind = switch (o) {
            case Line(Point _, Point(int x, int y)) when x == y -> "ends on the diagonal";
            case Line _ -> "a line";                                  // unnamed pattern variable
            default -> "other";
        };
        System.out.println(kind);
    }
}
