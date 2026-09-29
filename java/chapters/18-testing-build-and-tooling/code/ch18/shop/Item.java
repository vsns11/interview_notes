package ch18.shop;

import java.math.BigDecimal;
import java.util.Objects;

/** One line in a cart: a product and how many of it. */
public record Item(String sku, String name, BigDecimal unitPrice, int quantity) {
    public Item {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive: " + quantity);
        if (unitPrice.signum() < 0) throw new IllegalArgumentException("negative price: " + unitPrice);
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
