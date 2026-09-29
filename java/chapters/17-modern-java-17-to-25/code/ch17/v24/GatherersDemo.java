package ch17.v24;

import java.util.stream.Gatherers;
import java.util.stream.Stream;

/** JEP 485 (Java 24): custom intermediate operations via Stream::gather. Details: Chapter 12. */
public class GatherersDemo {
    public static void main(String[] args) {
        System.out.println(Stream.of(1, 2, 3, 4, 5, 6, 7).gather(Gatherers.windowFixed(3)).toList());
        System.out.println(Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowSliding(2)).toList());
        System.out.println(Stream.of(1, 2, 3, 4, 5).gather(Gatherers.scan(() -> 0, Integer::sum)).toList());
        System.out.println(Stream.of("a", "b", "c").gather(Gatherers.fold(() -> "", String::concat)).toList());
    }
}
