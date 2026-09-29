package lld.ch04.checkout.fin;

import java.math.BigDecimal;
import lld.ch04.checkout.Order;

@FunctionalInterface
public interface OrderNotifier {
    void send(Order order, BigDecimal total);
}
