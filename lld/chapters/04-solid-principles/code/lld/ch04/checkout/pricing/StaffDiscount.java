package lld.ch04.checkout.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StaffDiscount implements PricingRule {
    private static final BigDecimal RATE = new BigDecimal("0.70");

    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.multiply(RATE).setScale(2, RoundingMode.HALF_EVEN);
    }
}
