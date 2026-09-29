package lld.ch05.immutability;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Immutability: once created, an object's observable state never changes. */
public class Main {

    // ---------------- BEFORE: a mutable class that leaks its internals ----------------
    static class Before {
        static class Itinerary {
            private final List<String> cities;               // 'final' reference, but the LIST is mutable
            private LocalDate start;
            Itinerary(List<String> cities, LocalDate start) { this.cities = cities; this.start = start; }
            List<String> getCities() { return cities; }      // hands out the internal list
            void setStart(LocalDate d) { start = d; }
            @Override public String toString() { return start + " " + cities; }
        }
    }

    // ---------------- AFTER: final class, final fields, defensive copies, "wither" methods ----------------
    static class After {
        /** A record is shallowly immutable; List.copyOf makes the list part immutable too. */
        record Itinerary(List<String> cities, LocalDate start) {
            Itinerary {
                cities = List.copyOf(cities);   // copy in: caller's later edits cannot reach us
            }
            Itinerary withStart(LocalDate newStart) { return new Itinerary(cities, newStart); }
            Itinerary plusCity(String c) {
                List<String> next = new ArrayList<>(cities);
                next.add(c);
                return new Itinerary(next, start);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("== BEFORE ==");
        List<String> input = new ArrayList<>(List.of("Delhi", "Agra"));
        var trip = new Before.Itinerary(input, LocalDate.of(2026, 10, 1));
        input.add("Jaipur");                          // caller edits its own list...
        trip.getCities().clear();                     // ...another caller wipes ours
        System.out.println("shared trip is now: " + trip + "   <-- changed by outsiders");

        System.out.println("== AFTER ==");
        List<String> input2 = new ArrayList<>(List.of("Delhi", "Agra"));
        var t1 = new After.Itinerary(input2, LocalDate.of(2026, 10, 1));
        input2.add("Jaipur");
        try { t1.cities().clear(); } catch (UnsupportedOperationException e) { System.out.println("cannot clear: unmodifiable"); }
        var t2 = t1.plusCity("Jaipur").withStart(LocalDate.of(2026, 11, 5));
        System.out.println("t1 = " + t1);
        System.out.println("t2 = " + t2);

        System.out.println("== JDK immutables ==");
        String s = "hello";
        String upper = s.toUpperCase();
        LocalDate d = LocalDate.of(2026, 1, 31);
        System.out.println(s + " / " + upper + " ; " + d + " plusMonths(1) = " + d.plusMonths(1) + " (d unchanged: " + d + ")");
    }
}
