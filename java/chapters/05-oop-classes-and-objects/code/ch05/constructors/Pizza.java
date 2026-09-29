package ch05.constructors;

import java.util.ArrayList;
import java.util.List;

public class Pizza {
    private final String size;
    private final boolean extraCheese;
    private final List<String> toppings = new ArrayList<>();

    // The "full" constructor: every other constructor delegates here with this(...)
    public Pizza(String size, boolean extraCheese, String... toppings) {
        if (!List.of("S", "M", "L").contains(size))
            throw new IllegalArgumentException("bad size: " + size);
        this.size = size;                 // 'this.size' = the field, 'size' = the parameter
        this.extraCheese = extraCheese;
        this.toppings.addAll(List.of(toppings));
    }

    public Pizza(String size) {
        this(size, false);                // constructor chaining: must be the only call to this()/super()
    }

    public Pizza() {
        this("M");
    }

    // Java 25 (JEP 513, flexible constructor bodies): statements may come BEFORE this(...)/super(...)
    // as long as they do not use 'this'. Here we parse and validate before delegating.
    public Pizza(String order, char separator) {
        String[] parts = order.strip().split(String.valueOf(separator));
        if (parts.length == 0 || parts[0].isEmpty())
            throw new IllegalArgumentException("empty order");
        this(parts[0].toUpperCase(), false, java.util.Arrays.copyOfRange(parts, 1, parts.length));
    }

    public Pizza addTopping(String t) {   // returning 'this' allows call chaining
        toppings.add(t);
        return this;
    }

    @Override
    public String toString() {
        return "Pizza[" + size + (extraCheese ? ", extra cheese" : "") + ", " + toppings + "]";
    }
}
