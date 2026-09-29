package ch12.ops;

import java.util.*;
import java.util.stream.*;

/** Every intermediate operation you need day to day. */
public class IntermediateOps {
    record Book(String title, List<String> tags, int year) {}

    public static void main(String[] args) {
        List<Integer> nums = List.of(5, 3, 8, 3, 1, 9, 8, 2);
        System.out.println("filter even      " + nums.stream().filter(n -> n % 2 == 0).toList());
        System.out.println("map x10          " + nums.stream().map(n -> n * 10).toList());
        System.out.println("distinct         " + nums.stream().distinct().toList());
        System.out.println("sorted           " + nums.stream().sorted().toList());
        System.out.println("sorted desc      " + nums.stream().sorted(Comparator.reverseOrder()).toList());
        System.out.println("skip 2 limit 3   " + nums.stream().skip(2).limit(3).toList());
        System.out.println("takeWhile <8     " + nums.stream().takeWhile(n -> n < 8).toList() + "   (Java 9: stops at first failure)");
        System.out.println("dropWhile <8     " + nums.stream().dropWhile(n -> n < 8).toList());
        System.out.println("filter <8        " + nums.stream().filter(n -> n < 8).toList() + "   (compare: looks at every element)");

        // flatMap: each element -> a stream of 0..n elements, flattened into one stream
        List<Book> books = List.of(
                new Book("Effective Java", List.of("java", "best-practices"), 2018),
                new Book("Modern Java in Action", List.of("java", "streams", "fp"), 2018),
                new Book("DDIA", List.of("databases", "distributed"), 2017));
        System.out.println("map (nested)     " + books.stream().map(Book::tags).toList());
        System.out.println("flatMap          " + books.stream().flatMap(b -> b.tags().stream()).distinct().sorted().toList());
        System.out.println("flatMap words    " + Stream.of("to be", "or not").flatMap(line -> Arrays.stream(line.split(" "))).toList());

        // mapMulti (Java 16): push 0..n results through a consumer -- no intermediate streams
        List<String> tagged = books.stream()
                .<String>mapMulti((b, sink) -> { for (String t : b.tags()) if (t.startsWith("d")) sink.accept(b.title() + ":" + t); })
                .toList();
        System.out.println("mapMulti         " + tagged);

        // map to a different type, and to primitive streams
        System.out.println("mapToInt sum     " + books.stream().mapToInt(Book::year).sum());

        // peek is for debugging only
        List<String> seen = new ArrayList<>();
        List<String> titles = books.stream().map(Book::title).peek(seen::add).filter(t -> t.length() < 16).toList();
        System.out.println("peek saw " + seen.size() + " titles, kept " + titles);

        // sorted with a comparator; distinct uses equals/hashCode
        System.out.println("sorted by length " + Stream.of("ccc", "a", "bb").sorted(Comparator.comparingInt(String::length)).toList());
        System.out.println("boxed / asLong   " + IntStream.of(1, 2).asLongStream().boxed().toList());
        System.out.println("unordered/parallel/sequential/onClose also exist (see 12.8)");
    }
}
