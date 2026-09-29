package lld.ch04.checkout.fin;

import java.math.BigDecimal;
import lld.ch04.checkout.Order;

/** STEP 4 (ISP): the role the checkout needs -- one method wide, owned by the checkout. */
@FunctionalInterface
public interface OrderSaver {
    void save(Order order, BigDecimal total);
}
