package lld.ch04.lsp.rules;

import java.math.BigDecimal;

/**
 * HISTORY CONSTRAINT BROKEN: adds a mutator, so an object the caller believed was
 * frozen can now change between two reads. No single method call looks wrong.
 */
public class AdjustablePrice extends Price {
    private BigDecimal current;

    public AdjustablePrice(BigDecimal value) {
        super(value);
        this.current = value;
    }

    @Override
    public BigDecimal value() {
        return current;
    }

    public void adjust(BigDecimal newValue) {
        this.current = newValue;
    }
}
