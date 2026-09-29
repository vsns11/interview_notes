package ch09.bounds;

import java.util.List;

public class Bounded {

    // Upper bound: T must be Number or a subclass, so Number's methods are available
    static <T extends Number> double sum(List<T> values) {
        double total = 0;
        for (T v : values) total += v.doubleValue();
        return total;
    }

    // Recursive bound: T must be comparable to itself (the Collections.max pattern)
    static <T extends Comparable<? super T>> T max(List<? extends T> list) {
        T best = list.get(0);
        for (T x : list) if (x.compareTo(best) > 0) best = x;
        return best;
    }

    // Multiple bounds: class first, then interfaces, joined with &
    static <T extends Number & Comparable<T>> T clamp(T value, T lo, T hi) {
        if (value.compareTo(lo) < 0) return lo;
        if (value.compareTo(hi) > 0) return hi;
        return value;
    }

    // A bounded generic class
    static class Stats<T extends Number> {
        private final List<T> data;
        Stats(List<T> data) { this.data = data; }
        double average() { return data.isEmpty() ? 0 : sum(data) / data.size(); }
    }

    public static void main(String[] args) {
        System.out.println("sum ints=" + sum(List.of(1, 2, 3)) + " sum doubles=" + sum(List.of(1.5, 2.5)));
        // sum(List.of("a"));            // compile error: String is not within bound Number
        System.out.println("max words=" + max(List.of("pear", "apple", "zucchini")) + " max ints=" + max(List.of(3, 9, 4)));
        System.out.println("clamp(15, 0, 10)=" + clamp(15, 0, 10) + " clamp(2.5, 0.0, 1.0)=" + clamp(2.5, 0.0, 1.0));
        System.out.println("Stats average=" + new Stats<>(List.of(2, 4, 9)).average());
        // new Stats<String>(...)        // compile error: type argument String is not within bounds
    }
}
