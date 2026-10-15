package lld.ch11.architecture.domain;

import java.math.BigDecimal;

/** Domain events: facts the core announces. Past tense, immutable. */
public sealed interface OrderEvent permits OrderEvent.OrderPaid, OrderEvent.PaymentFailed {
    String orderId();

    record OrderPaid(String orderId, String customerId, BigDecimal total) implements OrderEvent {}
    record PaymentFailed(String orderId, String customerId, BigDecimal total) implements OrderEvent {}
}
