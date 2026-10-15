package lld.ch11.architecture.domain;

import java.math.BigDecimal;

/** Value object: one priced line of an order. Immutable, compared by value. */
public record OrderLine(String sku, int qty, BigDecimal unitPrice) {
    public OrderLine {
        if (qty <= 0) throw new IllegalArgumentException("quantity must be positive for " + sku);
        if (unitPrice.signum() < 0) throw new IllegalArgumentException("negative price for " + sku);
    }

    public BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(qty)); }
}
