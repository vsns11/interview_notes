package lld.ch11.architecture.adapter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lld.ch11.architecture.domain.Order;
import lld.ch11.architecture.port.OrderRepository;

/** DRIVEN adapter: in memory. A JDBC adapter would implement the same port. */
public final class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> byId = new ConcurrentHashMap<>();

    public void save(Order order) { byId.put(order.id(), order); }
    public Optional<Order> findById(String id) { return Optional.ofNullable(byId.get(id)); }
}
