package lld.ch02.immutability;

import java.util.List;

/**
 * A RECORD: the compiler generates the private final fields, canonical constructor, accessors,
 * equals, hashCode and toString. The compact constructor validates and makes a DEFENSIVE COPY,
 * because a record is only shallowly immutable.
 */
public record Order(String id, List<String> items, Money total) {
    public Order {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        items = List.copyOf(items);                 // unmodifiable copy: caller's list cannot leak in
    }

    public Order withItem(String item, Money price) {  // "wither": returns a modified copy
        var more = new java.util.ArrayList<>(items);
        more.add(item);
        return new Order(id, more, total.plus(price));
    }
}
