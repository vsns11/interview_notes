package ch10.basics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public record Order(long id, String owner, int cents) {}

    private final List<Order> orders = List.of(
            new Order(1, "alice", 1999), new Order(2, "carol", 500), new Order(3, "alice", 250));

    // Runs BEFORE the method: only the owner or an admin may list someone's orders
    @PreAuthorize("#owner == authentication.name or hasRole('ADMIN')")
    public List<Order> ordersOf(String owner) {
        return orders.stream().filter(o -> o.owner().equals(owner)).toList();
    }

    // Runs AFTER the method: the returned object decides (useful when you must load it first)
    @PostAuthorize("returnObject.owner() == authentication.name")
    public Order find(long id) {
        return orders.stream().filter(o -> o.id() == id).findFirst().orElseThrow();
    }

    // Filters the returned collection element by element (fine for small lists only)
    @PostFilter("filterObject.owner() == authentication.name or hasRole('ADMIN')")
    public List<Order> all() {
        return new ArrayList<>(orders); // must be mutable
    }

    // Fine-grained authority instead of a role
    @PreAuthorize("hasAuthority('order:refund')")
    public String refund(long id) {
        return "refunded " + id;
    }
}
