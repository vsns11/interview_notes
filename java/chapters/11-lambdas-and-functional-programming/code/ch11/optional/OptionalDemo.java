package ch11.optional;

import java.util.*;

/** Optional: creation, transformation, extraction -- and the misuses to avoid. */
public class OptionalDemo {
    record Address(String city, String zip) {}
    record Customer(String name, Address address, String email) {
        Optional<Address> addressOpt() { return Optional.ofNullable(address); }
        Optional<String> emailOpt() { return Optional.ofNullable(email); }
    }

    static final Map<Integer, Customer> DB = Map.of(
            1, new Customer("Ana", new Address("Lisbon", "1100"), "ana@example.com"),
            2, new Customer("Bo", null, null));

    /** Good use: a return type that says "there may be no result". */
    static Optional<Customer> findById(int id) { return Optional.ofNullable(DB.get(id)); }

    static String expensiveDefault() { System.out.print("(computing default) "); return "Unknown"; }

    public static void main(String[] args) {
        // Creating
        Optional<String> some = Optional.of("hi");
        Optional<String> none = Optional.empty();
        Optional<String> maybe = Optional.ofNullable(null);
        System.out.println(some + " " + none + " " + maybe + " isPresent=" + some.isPresent() + " isEmpty=" + none.isEmpty());
        try { Optional.of(null); } catch (NullPointerException e) { System.out.println("Optional.of(null) -> NPE"); }

        // Transforming: map, flatMap, filter -- no null checks
        String city1 = findById(1).flatMap(Customer::addressOpt).map(Address::city).orElse("no city");
        String city2 = findById(2).flatMap(Customer::addressOpt).map(Address::city).orElse("no city");
        String city3 = findById(9).flatMap(Customer::addressOpt).map(Address::city).orElse("no city");
        System.out.println("cities: " + city1 + ", " + city2 + ", " + city3);
        System.out.println("gmail? " + findById(1).flatMap(Customer::emailOpt).filter(e -> e.endsWith("@gmail.com")).isPresent());

        // Extracting: orElse is EAGER, orElseGet is LAZY
        System.out.print("orElse on present:    ");
        System.out.println(findById(1).map(Customer::name).orElse(expensiveDefault()));
        System.out.print("orElseGet on present: ");
        System.out.println(findById(1).map(Customer::name).orElseGet(OptionalDemo::expensiveDefault));

        try { findById(9).orElseThrow(); } catch (NoSuchElementException e) { System.out.println("orElseThrow() -> " + e.getMessage()); }
        try { findById(9).orElseThrow(() -> new IllegalArgumentException("customer 9 not found")); }
        catch (IllegalArgumentException e) { System.out.println("orElseThrow(supplier) -> " + e.getMessage()); }

        // Acting: ifPresent, ifPresentOrElse (9), or (9), stream (9)
        findById(2).flatMap(Customer::emailOpt).ifPresentOrElse(
                e -> System.out.println("mail " + e), () -> System.out.println("Bo has no email -> send SMS"));
        Optional<String> contact = findById(2).flatMap(Customer::emailOpt).or(() -> Optional.of("sms:+000"));
        System.out.println("fallback Optional via or(): " + contact.get());
        List<String> emails = List.of(1, 2, 9).stream().map(OptionalDemo::findById)
                .flatMap(Optional::stream).flatMap(c -> c.emailOpt().stream()).toList();
        System.out.println("all known emails: " + emails);

        // Primitive Optionals avoid boxing
        OptionalInt maxLen = List.of("a", "abc", "ab").stream().mapToInt(String::length).max();
        System.out.println("OptionalInt max=" + maxLen.getAsInt() + ", empty average=" + java.util.stream.IntStream.empty().average());

        // Anti-patterns (compile fine, read badly)
        Optional<String> opt = findById(1).map(Customer::name);
        if (opt.isPresent()) System.out.println("anti-pattern isPresent+get: " + opt.get());   // just a verbose null check
        System.out.println("better: " + opt.orElse("?"));
    }
}
