package lld.ch19.amazon;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Money helpers: always BigDecimal, always 2 decimal places, never double. */
final class Money {
    private Money() { }

    static final BigDecimal ZERO = of("0");

    static BigDecimal of(String amount) { return new BigDecimal(amount).setScale(2, RoundingMode.HALF_EVEN); }

    static BigDecimal of(BigDecimal amount) { return amount.setScale(2, RoundingMode.HALF_EVEN); }

    static String fmt(BigDecimal amount) { return "$" + amount.setScale(2, RoundingMode.HALF_EVEN).toPlainString(); }
}
