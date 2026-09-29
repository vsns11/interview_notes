package lld.ch04.checkout.fin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lld.ch04.checkout.Order;

/** The full repository grows (save, find, list...) but plays two separate roles. */
public class SqlOrderRepository implements OrderSaver, OrderFinder {
    private final List<Order> table = new ArrayList<>(); // stands in for a real DB table

    @Override
    public void save(Order order, BigDecimal total) {
        table.add(order);
        System.out.println("  INSERT INTO orders VALUES ('" + order.orderId() + "', " + total + ")");
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return table.stream().filter(o -> o.orderId().equals(orderId)).findFirst();
    }

    @Override
    public List<Order> listAll() {
        return List.copyOf(table);
    }
}
