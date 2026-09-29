package lld.ch04.lsp.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Contract -- precondition: any amount >= 0. Postcondition: result = 90% of amount. */
public class Discount {
    private static final BigDecimal RATE = new BigDecimal("0.90");

    public BigDecimal apply(BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        return amount.multiply(RATE).setScale(2, RoundingMode.HALF_EVEN);
    }
}
