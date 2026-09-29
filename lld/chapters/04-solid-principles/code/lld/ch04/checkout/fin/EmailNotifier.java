package lld.ch04.checkout.fin;

import java.math.BigDecimal;
import lld.ch04.checkout.Order;

public class EmailNotifier implements OrderNotifier {
    @Override
    public void send(Order order, BigDecimal total) {
        System.out.println("  emailing " + order.email() + ": total " + total);
    }
}
