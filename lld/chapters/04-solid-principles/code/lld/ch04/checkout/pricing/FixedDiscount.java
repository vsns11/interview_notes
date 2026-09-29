package lld.ch04.checkout.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** STEP 3 fix: keeps the promise that a total is never negative. */
public class FixedDiscount implements PricingRule {
    private final BigDecimal off;

    public FixedDiscount(BigDecimal off) {
        if (off.signum() < 0) {
            throw new IllegalArgumentException("discount must be >= 0");
        }
        this.off = off;
    }

    @Override
    public BigDecimal apply(BigDecimal amount) {
        return amount.subtract(off).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_EVEN);
    }
}
