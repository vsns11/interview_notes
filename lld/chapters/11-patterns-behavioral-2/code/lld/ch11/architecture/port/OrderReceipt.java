package lld.ch11.architecture.port;

import java.math.BigDecimal;
import lld.ch11.architecture.domain.OrderStatus;

/** Output of the driving port: a read-only view, so callers never hold the mutable Order. */
public record OrderReceipt(String orderId, OrderStatus status, BigDecimal total) {}
