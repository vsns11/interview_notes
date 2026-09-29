package ch09.jdbc;

import java.math.BigDecimal;

/** Part of the PurchaseOrder aggregate: no @Id, no repository, lives and dies with its order. */
public record OrderLine(String sku, int quantity, BigDecimal unitPrice) {}
