package lld.ch02.composition;

import java.util.List;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        InheritingCountingSet<String> bad = new InheritingCountingSet<>();
        bad.addAll(List.of("a", "b", "c"));
        System.out.println("inheritance: added 3, attempts counted = " + bad.attempts());

        CountingSet<String> good = new CountingSet<>(new TreeSet<>());
        good.addAll(List.of("a", "b", "c"));
        good.add("a");
        System.out.println("composition: 4 attempts counted = " + good.attempts() + ", size = " + good.size());

        Car car = new Car("City", () -> "petrol engine vroom");
        System.out.println(car.drive());
        car.replaceEngine(() -> "electric motor hum");
        System.out.println(car.drive());
    }
}
