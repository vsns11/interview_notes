package ch06.records;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecordsDemo {

    // One line gives: private final fields, canonical constructor, accessors x() and y(),
    // equals, hashCode and toString based on all components.
    record Point(int x, int y) {
        static final Point ORIGIN = new Point(0, 0);                  // static fields are allowed
        Point translate(int dx, int dy) { return new Point(x + dx, y + dy); }   // "wither"-style method
        double distanceTo(Point o) { return Math.hypot(x - o.x, y - o.y); }
    }

    record Range(int start, int end) {
        Range {                                                       // compact canonical constructor
            if (start > end) throw new IllegalArgumentException(start + " > " + end);
        }                                                             // fields are assigned automatically after this
        Range(int single) { this(single, single); }                   // extra constructors must delegate
        int length() { return end - start; }
    }

    record Team(String name, List<String> members) {
        Team {
            members = List.copyOf(members);                           // defensive copy: reassign the PARAMETER
        }
    }

    record Temperature(double celsius) {
        @Override public String toString() { return celsius + "°C"; }   // any generated member can be overridden
        static Temperature ofFahrenheit(double f) { return new Temperature((f - 32) * 5 / 9); }  // static factory
    }

    public static void main(String[] args) {
        Point p = new Point(3, 4);
        System.out.println(p + " x=" + p.x() + " dist=" + p.distanceTo(Point.ORIGIN) + " moved=" + p.translate(1, 1));
        System.out.println("equals by value: " + p.equals(new Point(3, 4))
                + ", set size: " + new HashSet<>(List.of(p, new Point(3, 4))).size());

        System.out.println(new Range(2, 7) + " length " + new Range(2, 7).length() + ", " + new Range(5));
        try {
            new Range(9, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        List<String> people = new java.util.ArrayList<>(List.of("Ana", "Bo"));
        Team t = new Team("Core", people);
        people.add("Mallory");
        System.out.println(t + " (unchanged by the caller's add)");

        System.out.println(Temperature.ofFahrenheit(212));

        record Pair(String key, int count) {}                       // local record (Java 16)
        Set<Pair> pairs = Set.of(new Pair("a", 1));
        System.out.println("local record: " + pairs);
    }
}
