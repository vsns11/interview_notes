package lld.ch04.checkout.step1;

import lld.ch04.checkout.step0.CustomerOrder;

/** Actor: business-rules owner. */
public class OrderValidator {
    public boolean isValid(CustomerOrder order) {
        return order.amount().signum() > 0;
    }
}
