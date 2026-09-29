package ch11.methodrefs;

import java.util.*;
import java.util.function.*;

/** The four kinds of method reference and their lambda equivalents. */
public class MethodRefDemo {
    record User(String name, int age) {
        boolean isAdult() { return age >= 18; }
        static int byAge(User a, User b) { return Integer.compare(a.age, b.age); }
    }

    public static void main(String[] args) {
        // 1. Static method:              Class::staticMethod      (args) -> Class.staticMethod(args)
        Function<String, Integer> parse = Integer::parseInt;
        Comparator<User> ageOrder = User::byAge;

        // 2. Bound instance method:      object::method           (args) -> object.method(args)
        String prefix = "ID-";
        Function<String, String> addPrefix = prefix::concat;
        Consumer<Object> printer = System.out::println;

        // 3. Unbound instance method:    Class::instanceMethod    (obj, args) -> obj.method(args)
        Function<String, String> upper = String::toUpperCase;      // s -> s.toUpperCase()
        Predicate<User> adult = User::isAdult;                     // u -> u.isAdult()
        BiFunction<String, String, Boolean> startsWith = String::startsWith; // (s, p) -> s.startsWith(p)

        // 4. Constructor:                Class::new               (args) -> new Class(args)
        Supplier<List<User>> listMaker = ArrayList::new;
        BiFunction<String, Integer, User> makeUser = User::new;
        IntFunction<int[]> arrayMaker = int[]::new;               // array constructor

        List<User> users = listMaker.get();
        users.add(makeUser.apply("Zed", 17)); users.add(makeUser.apply("Amy", 34)); users.add(makeUser.apply("Kim", 25));
        users.sort(ageOrder);
        printer.accept(users);
        System.out.println(parse.apply("42") + 1 + " " + addPrefix.apply("7") + " " + upper.apply("ok")
                + " adult(Zed)=" + adult.test(users.get(0)) + " startsWith=" + startsWith.apply("java", "ja")
                + " array length=" + arrayMaker.apply(5).length);

        // Same reference, different shapes: String::length fits Function<String,Integer> and ToIntFunction<String>
        ToIntFunction<String> len1 = String::length;
        Function<String, Integer> len2 = String::length;
        System.out.println(len1.applyAsInt("four") + " " + len2.apply("three"));

        // Bound reference evaluates its receiver ONCE, when the reference is created
        StringBuilder sb = new StringBuilder("v1");
        Supplier<String> snap = sb::toString;
        sb.append("+v2");
        System.out.println("bound receiver is the same object: " + snap.get());

        // Comparator.comparing with method references reads like a sentence
        users.sort(Comparator.comparing(User::name));
        users.forEach(u -> System.out.print(u.name() + " "));
        System.out.println();
    }
}
