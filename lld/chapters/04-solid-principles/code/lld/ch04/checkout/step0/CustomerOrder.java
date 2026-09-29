package lld.ch04.checkout.step0;

import java.math.BigDecimal;

/** The original order shape: the customer type travels with the order as a string. */
public record CustomerOrder(String orderId, String customerType, BigDecimal amount, String email) { }
