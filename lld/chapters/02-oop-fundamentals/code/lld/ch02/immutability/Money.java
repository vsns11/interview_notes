package lld.ch02.immutability;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A hand-written immutable VALUE OBJECT: final class, private final fields, no setters,
 * "changes" return NEW objects. The amount is a BigDecimal (exact decimal arithmetic, never double),
 * normalised to 2 decimal places so that 5 and 5.00 are the same Money.
 */
public final class Money {
    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        this.currency = Objects.requireNonNull(currency);
        // UNNECESSARY = refuse (ArithmeticException) anything finer than a cent instead of silently rounding it
        this.amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money of(String amount, String currency) { return new Money(new BigDecimal(amount), currency); }

    public Money plus(Money other) {
        if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
        return new Money(amount.add(other.amount), currency);   // new object; 'this' is untouched
    }

    public BigDecimal amount() { return amount; }

    @Override public boolean equals(Object o) {        // safe: the scale is always 2, so equals == numeric equality
        return o instanceof Money m && amount.equals(m.amount) && currency.equals(m.currency);
    }
    @Override public int hashCode() { return Objects.hash(amount, currency); }
    @Override public String toString() { return amount.toPlainString() + " " + currency; }
}
