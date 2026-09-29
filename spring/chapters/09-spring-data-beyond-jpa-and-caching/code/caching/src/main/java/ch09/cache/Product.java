package ch09.cache;

import java.math.BigDecimal;

/** Immutable: safe to share from a cache between callers. */
public record Product(String sku, String name, BigDecimal price) {}
