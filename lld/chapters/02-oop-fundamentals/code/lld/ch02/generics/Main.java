package lld.ch02.generics;

import java.util.ArrayList;
import java.util.List;

public class Main {
    /** Generic METHOD with a bounded type parameter. */
    static <T extends Comparable<? super T>> T max(List<? extends T> items) {
        T best = items.get(0);
        for (T t : items) if (t.compareTo(best) > 0) best = t;
        return best;
    }

    /** PECS: Producer Extends (we read from src), Consumer Super (we write into dst). */
    static <T> void copy(List<? extends T> src, List<? super T> dst) {
        dst.addAll(src);
    }

    public static void main(String[] args) {
        Repository<String, User> users = new Repository<>();
        users.save(new User("u1", "Ada"));
        users.save(new User("u2", "Kai"));
        System.out.println("user u2: " + users.findById("u2").map(User::name).orElse("none"));
        System.out.println("user u9: " + users.findById("u9").map(User::name).orElse("none"));

        Repository<Integer, Product> products = new Repository<>();
        products.save(new Product(1, "pen", 150));
        products.save(new Product(2, "lamp", 2_500));
        products.save(new Product(3, "mug", 800));
        System.out.println("most expensive: " + max(products.findAll()).title());

        List<Integer> ints = List.of(3, 1, 2);
        List<Number> numbers = new ArrayList<>();
        copy(ints, numbers);                            // Integer list into a Number list
        System.out.println("copied into List<Number>: " + numbers + ", max int = " + max(ints));

        // users.save(new Product(4, "x", 1));          // would NOT compile: type safety at compile time
        List<String> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        System.out.println("same runtime class (type erasure)? " + (a.getClass() == b.getClass()));
    }
}
