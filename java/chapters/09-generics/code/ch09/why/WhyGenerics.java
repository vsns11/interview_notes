package ch09.why;

import java.util.ArrayList;
import java.util.List;

public class WhyGenerics {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {
        // Before Java 5: a raw List holds Object, so every read needs a cast
        List raw = new ArrayList();
        raw.add("Ada");
        raw.add(42);                                  // compiles: nothing stops a wrong type going in
        try {
            for (Object o : raw) {
                String s = (String) o;                // fails at RUN time on the Integer
                System.out.println("raw: " + s.toUpperCase());
            }
        } catch (ClassCastException e) {
            System.out.println("raw list -> ClassCastException: " + e.getMessage());
        }

        // With generics: the compiler checks every add, and no casts are needed on get
        List<String> names = new ArrayList<>();       // diamond <> (Java 7) infers <String>
        names.add("Ada");
        // names.add(42);                             // compile error: incompatible types: int cannot be converted to String
        String first = names.get(0);                  // no cast
        System.out.println("generic: " + first.toUpperCase());
    }
}
