package ch18.shop;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A "fake": a real, working implementation that is simply too simple for production. */
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> byId = new LinkedHashMap<>();

    @Override public void save(Order order)               { byId.put(order.id(), order); }
    @Override public Optional<Order> findById(String id)  { return Optional.ofNullable(byId.get(id)); }
    @Override public List<Order> findByCustomer(String c) {
        List<Order> out = new ArrayList<>();
        for (Order o : byId.values()) if (o.customer().equals(c)) out.add(o);
        return out;
    }
}
