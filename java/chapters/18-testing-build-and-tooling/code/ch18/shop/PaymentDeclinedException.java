package ch18.shop;

import java.math.BigDecimal;

public class PaymentDeclinedException extends RuntimeException {
    public PaymentDeclinedException(String customer, BigDecimal amount) {
        super("payment of " + amount + " declined for " + customer);
    }
}
