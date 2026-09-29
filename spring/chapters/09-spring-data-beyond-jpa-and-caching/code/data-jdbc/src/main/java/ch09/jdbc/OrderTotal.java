package ch09.jdbc;

import java.math.BigDecimal;

/** Read model (DTO) filled from a custom @Query against the order_totals view. */
public record OrderTotal(Long orderId, String customer, BigDecimal total) {}
