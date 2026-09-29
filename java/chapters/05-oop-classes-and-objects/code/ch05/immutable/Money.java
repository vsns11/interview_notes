package ch05.immutable;

import java.util.Objects;

// An immutable value class, built by hand (Chapter 6 shows the shorter 'record' form).
public final class Money {                            // 1. final: no subclass can add mutable state
    private final long cents;                          // 2. private final fields
    private final String currency;

    public Money(long cents, String currency) {        // 3. validate once, in the constructor
        this.cents = cents;
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public long cents() { return cents; }              // 4. getters only, no setters
    public String currency() { return currency; }

    public Money plus(Money other) {                   // 5. "changes" return NEW objects
        if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
        return new Money(cents + other.cents, currency);
    }

    @Override public String toString() { return String.format("%d.%02d %s", cents / 100, cents % 100, currency); }
}
