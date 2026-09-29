package lld.ch04.checkout.step2;

import java.math.BigDecimal;
import lld.ch04.checkout.Order;
import lld.ch04.checkout.pricing.PricingRule;

/** STEP 2: no branch left. The calculator never learns how many rates exist. */
public class PriceCalculator {
    public BigDecimal total(Order order, PricingRule rule) {
        return rule.apply(order.amount());
    }
}
