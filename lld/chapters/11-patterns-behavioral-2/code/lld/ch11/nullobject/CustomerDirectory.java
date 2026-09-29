package lld.ch11.nullobject;

import java.util.Map;
import java.util.Optional;

public final class CustomerDirectory {
    private final Map<String, Customer> byId = Map.of(
        "c1", new RegisteredCustomer("Asha", 10, "asha@example.com"));

    /** Style 1: never return null; return the Null Object instead. */
    public Customer find(String id) { return byId.getOrDefault(id, GuestCustomer.INSTANCE); }

    /** Style 2: make absence explicit in the type; the caller chooses the fallback. */
    public Optional<Customer> lookup(String id) { return Optional.ofNullable(byId.get(id)); }
}
