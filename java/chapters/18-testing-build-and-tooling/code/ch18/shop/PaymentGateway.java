package ch18.shop;

import java.math.BigDecimal;

/** The outside world (a card processor). Tests replace it with a mock. */
public interface PaymentGateway {
    PaymentResult charge(String customer, BigDecimal amount);

    record PaymentResult(boolean approved, String reference) {}
}
