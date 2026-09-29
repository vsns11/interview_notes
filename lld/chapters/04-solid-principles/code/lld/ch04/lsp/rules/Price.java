package lld.ch04.lsp.rules;

import java.math.BigDecimal;

/** Contract (history): once created, a Price never changes value. */
public class Price {
    private final BigDecimal value;

    public Price(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal value() {
        return value;
    }
}
