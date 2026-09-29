package ch12.mistakes;

import java.util.*;
import java.util.stream.*;

/** Common stream mistakes, each shown next to the fix. */
public class StreamMistakes {
    public static void main(String[] args) {
        // 1. Stream.of(int[]) is a Stream<int[]> with ONE element
        int[] arr = {1, 2, 3};
        System.out.println("1. Stream.of(int[]).count() = " + Stream.of(arr).count() + "  vs Arrays.stream(arr).count() = " + Arrays.stream(arr).count());

        // 2. Side effects into an outside collection instead of collecting
        List<String> out = new ArrayList<>();
        Stream.of("a", "b").map(String::toUpperCase).forEach(out::add);              // works sequentially, breaks in parallel
        List<String> fixed = Stream.of("a", "b").map(String::toUpperCase).toList();  // the fix
        System.out.println("2. forEach(out::add) " + out + " -> prefer toList() " + fixed);

        // 3. Modifying the source while streaming it
        List<Integer> src = new ArrayList<>(List.of(1, 2, 3));
        try { src.stream().forEach(x -> { if (x == 2) src.add(99); }); }
        catch (ConcurrentModificationException e) { System.out.println("3. modifying source -> ConcurrentModificationException"); }

        // 4. toMap with duplicate keys and no merge function
        try { Stream.of("apple", "avocado").collect(Collectors.toMap(s -> s.charAt(0), s -> s)); }
        catch (IllegalStateException e) { System.out.println("4. toMap duplicate -> IllegalStateException"); }

        // 5. toMap with a null value -> NPE (HashMap.merge rejects null values)
        try { Stream.of("x").collect(Collectors.toMap(s -> s, s -> (String) null)); }
        catch (NullPointerException e) { System.out.println("5. toMap null value -> NullPointerException"); }

        // 6. Expecting toList() to be mutable
        try { Stream.of(1).toList().add(2); }
        catch (UnsupportedOperationException e) { System.out.println("6. Stream.toList().add -> UnsupportedOperationException"); }

        // 7. sorted().findFirst() instead of min(): O(n log n) and a full buffer vs O(n)
        System.out.println("7. min() = " + Stream.of(4, 2, 9).min(Integer::compare).orElseThrow() + " (same answer as sorted().findFirst(), less work)");

        // 8. Infinite stream + an operation that needs all elements never finishes:
        //    Stream.iterate(1, i -> i + 1).sorted().limit(3)  // hangs -- put limit before sorted
        System.out.println("8. " + Stream.iterate(1, i -> i + 1).limit(3).sorted(Comparator.reverseOrder()).toList() + " (limit first)");

        // 9. distinct()+limit on an infinite stream that cannot produce enough distinct values hangs:
        //    Stream.generate(() -> 1).distinct().limit(2)     // waits forever for a second distinct value

        // 10. count() may skip executing the pipeline when it can compute the size directly (Java 9+)
        List<String> log = new ArrayList<>();
        long n = List.of("a", "b", "c").stream().peek(log::add).count();
        System.out.println("10. count()=" + n + " but peek ran " + log.size() + " times -- never put logic in peek");

        // 11. Checked exceptions inside lambdas: wrap them (see Chapter 11) or use a loop
        // 12. Overusing streams: a simple loop with early exit can be clearer
        int firstNegativeIndex = -1;
        int[] data = {3, 5, -2, 7};
        for (int i = 0; i < data.length; i++) if (data[i] < 0) { firstNegativeIndex = i; break; }
        int viaStream = IntStream.range(0, data.length).filter(i -> data[i] < 0).findFirst().orElse(-1);
        System.out.println("12. loop=" + firstNegativeIndex + " stream=" + viaStream + " (both fine -- choose the clearer one)");
    }
}
