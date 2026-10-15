package lld.ch11.architecture.port;

import java.util.Optional;
import lld.ch11.architecture.domain.Order;

/** DRIVEN (outbound) port: the core needs orders stored somewhere; it does not care where. */
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
}
