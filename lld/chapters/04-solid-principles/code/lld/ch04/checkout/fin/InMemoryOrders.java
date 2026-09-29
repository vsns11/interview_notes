package lld.ch04.checkout.fin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lld.ch04.checkout.Order;

/** Test double: one method to implement, because the role is one method wide. */
public class InMemoryOrders implements OrderSaver {
    private final List<String> saved = new ArrayList<>();

    @Override
    public void save(Order order, BigDecimal total) {
        saved.add(order.orderId() + "=" + total);
        System.out.println("  kept " + order.orderId() + " in memory");
    }

    public List<String> saved() {
        return List.copyOf(saved);
    }
}
