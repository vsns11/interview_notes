package lld.ch04.checkout.step1;

import java.math.BigDecimal;
import lld.ch04.checkout.step0.CustomerOrder;

/** Actor: database team. */
public class OrderRepository {
    public void save(CustomerOrder order, BigDecimal total) {
        System.out.println("INSERT INTO orders VALUES ('" + order.orderId() + "', " + total + ")");
    }
}
