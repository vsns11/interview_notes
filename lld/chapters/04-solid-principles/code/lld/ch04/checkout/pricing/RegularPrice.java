package lld.ch04.checkout.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RegularPrice implements PricingRule {
    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
