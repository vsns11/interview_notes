package ch07.jdbc;

import java.math.BigDecimal;

/** One row of the product table. Record components match column names (in_stock -> inStock). */
public record Product(Long id, String sku, String name, BigDecimal price, int inStock) {}
