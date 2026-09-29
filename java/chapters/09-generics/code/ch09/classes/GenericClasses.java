package ch09.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenericClasses {

    // A generic class: T is a type parameter, filled in by each use site
    static final class Box<T> {
        private T value;
        Box(T value) { this.value = value; }
        T get() { return value; }
        void set(T value) { this.value = value; }
        <R> Box<R> map(java.util.function.Function<? super T, ? extends R> f) {   // generic method inside a generic class
            return new Box<>(f.apply(value));
        }
        @Override public String toString() { return "Box[" + value + "]"; }
    }

    // Two type parameters, as a record (Java 16)
    record Pair<K, V>(K key, V value) {
        static <K, V> Pair<K, V> of(K k, V v) { return new Pair<>(k, v); }
        Pair<V, K> swap() { return new Pair<>(value, key); }
    }

    // A generic interface with two type parameters, and a generic implementation
    interface Repository<T, ID> {
        void save(ID id, T item);
        Optional<T> findById(ID id);
        List<T> findAll();
    }

    static class InMemoryRepository<T, ID> implements Repository<T, ID> {
        private final Map<ID, T> store = new HashMap<>();
        public void save(ID id, T item) { store.put(id, item); }
        public Optional<T> findById(ID id) { return Optional.ofNullable(store.get(id)); }
        public List<T> findAll() { return new ArrayList<>(store.values()); }
    }

    record Book(String title) {}

    // A class that fixes the type argument
    static class BookRepository extends InMemoryRepository<Book, String> {
        List<String> titles() { return findAll().stream().map(Book::title).sorted().toList(); }
    }

    public static void main(String[] args) {
        Box<String> s = new Box<>("hello");
        Box<Integer> len = s.map(String::length);
        System.out.println(s + " -> " + len + ", len.get() + 1 = " + (len.get() + 1));

        Pair<String, Integer> p = Pair.of("age", 36);
        System.out.println(p + " swapped " + p.swap());

        var nested = new Box<List<Pair<String, Integer>>>(List.of(p));   // types can nest
        System.out.println("nested: " + nested.get().get(0).key());

        BookRepository repo = new BookRepository();
        repo.save("978-0134685991", new Book("Effective Java"));
        repo.save("978-0321349606", new Book("Java Concurrency in Practice"));
        System.out.println("findById: " + repo.findById("978-0134685991").map(Book::title).orElse("?")
                + " | missing: " + repo.findById("x").isPresent() + " | titles: " + repo.titles());

        Repository<Integer, Long> numbers = new InMemoryRepository<>();
        numbers.save(1L, 100);
        System.out.println("numbers: " + numbers.findAll());
    }
}
