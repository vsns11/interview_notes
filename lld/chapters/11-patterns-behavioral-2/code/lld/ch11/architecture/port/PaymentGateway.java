package lld.ch11.architecture.port;

import java.math.BigDecimal;

/** DRIVEN port. One method, so a test can pass a lambda. */
@FunctionalInterface
public interface PaymentGateway {
    boolean charge(String customerId, BigDecimal amount);
}
