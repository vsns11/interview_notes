package ch18.shop;

import java.util.List;
import java.util.Optional;

/** Where orders are stored. Production: JDBC (ch18.jdbc). Tests: InMemoryOrderRepository. */
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
    List<Order> findByCustomer(String customer);
}
