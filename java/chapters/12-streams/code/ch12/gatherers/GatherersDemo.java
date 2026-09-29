package ch12.gatherers;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/** Stream gatherers (Java 24, JEP 485): custom intermediate operations. */
public class GatherersDemo {
    /** A custom gatherer: pass an element through only if its key hasn't been seen before (distinct by key). */
    static <T, K> Gatherer<T, ?, T> distinctBy(Function<? super T, ? extends K> key) {
        return Gatherer.ofSequential(
                HashSet<K>::new,                                              // initializer: private state
                Gatherer.Integrator.ofGreedy((seen, element, downstream) -> {  // integrator: per element
                    if (seen.add(key.apply(element))) return downstream.push(element);
                    return true;                                              // keep going
                }));
    }

    /** A short-circuiting gatherer: emit elements until their running total would exceed a budget. */
    static Gatherer<Integer, ?, Integer> takeWithinBudget(int budget) {
        return Gatherer.ofSequential(
                () -> new int[1],
                (spent, cost, downstream) -> {
                    if (spent[0] + cost > budget) return false;               // false = stop the whole stream
                    spent[0] += cost;
                    return downstream.push(cost);
                });
    }

    /** A gatherer with a finisher: emit runs of equal consecutive elements as lists. */
    static <T> Gatherer<T, ?, List<T>> runs() {
        class State { List<T> current = new ArrayList<>(); }
        return Gatherer.ofSequential(
                State::new,
                Gatherer.Integrator.ofGreedy((st, e, down) -> {
                    if (!st.current.isEmpty() && !st.current.getLast().equals(e)) { down.push(st.current); st.current = new ArrayList<>(); }
                    st.current.add(e);
                    return true;
                }),
                (st, down) -> { if (!st.current.isEmpty()) down.push(st.current); });   // finisher: flush last run
    }

    public static void main(String[] args) {
        List<Integer> temps = List.of(21, 23, 22, 25, 27, 26, 24);

        // Built-in gatherers in java.util.stream.Gatherers
        System.out.println("windowFixed(3)    " + temps.stream().gather(Gatherers.windowFixed(3)).toList());
        System.out.println("windowSliding(3)  " + temps.stream().gather(Gatherers.windowSliding(3)).toList());
        System.out.println("3-day moving avg  " + temps.stream().gather(Gatherers.windowSliding(3))
                .map(w -> String.format("%.1f", w.stream().mapToInt(Integer::intValue).average().orElseThrow())).toList());
        System.out.println("scan (running sum)" + Stream.of(5, 10, 20).gather(Gatherers.scan(() -> 0, Integer::sum)).toList());
        System.out.println("fold (one result) " + Stream.of("a", "b", "c").gather(Gatherers.fold(() -> "", (acc, s) -> acc + s)).toList());

        // mapConcurrent: run a (blocking) function on up to N virtual threads, keeping encounter order
        List<String> fetched = Stream.of("u1", "u2", "u3", "u4")
                .gather(Gatherers.mapConcurrent(2, url -> { sleep(20); return url.toUpperCase(); }))
                .toList();
        System.out.println("mapConcurrent(2)  " + fetched);

        // Custom gatherers
        record Visit(String user, String page) {}
        List<Visit> visits = List.of(new Visit("ana", "/home"), new Visit("bo", "/cart"), new Visit("ana", "/pay"), new Visit("cy", "/home"));
        System.out.println("distinctBy user   " + visits.stream().gather(distinctBy(Visit::user)).map(Visit::page).toList());
        System.out.println("within budget 50  " + Stream.of(10, 15, 20, 5, 30).gather(takeWithinBudget(50)).toList());
        System.out.println("runs              " + Stream.of("a", "a", "b", "c", "c", "c", "a").gather(runs()).toList());

        // Gatherers compose with andThen, like chained gather() calls
        Gatherer<Integer, ?, List<Integer>> pairsOfDistinct = GatherersDemo.<Integer, Integer>distinctBy(x -> x).andThen(Gatherers.windowFixed(2));
        System.out.println("andThen           " + Stream.of(1, 1, 2, 3, 3, 4, 5).gather(pairsOfDistinct).toList());
    }

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
