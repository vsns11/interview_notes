package lld.ch04.checkout.step1;

import java.math.BigDecimal;
import lld.ch04.checkout.step0.CustomerOrder;

/** Actor: marketing (owns the wording). */
public class Notifier {
    public void send(CustomerOrder order, BigDecimal total) {
        System.out.println("Emailing " + order.email() + ": your order total is " + total);
    }
}
