package lld.ch11.architecture.adapter;

import java.math.BigDecimal;
import lld.ch11.architecture.port.PaymentGateway;

/** DRIVEN adapter standing in for a card processor: approves charges up to a limit. */
public final class LimitPaymentGateway implements PaymentGateway {
    private final BigDecimal limit;

    public LimitPaymentGateway(BigDecimal limit) { this.limit = limit; }

    public boolean charge(String customerId, BigDecimal amount) {
        boolean ok = amount.compareTo(limit) <= 0;
        System.out.println("    [payment] charge " + customerId + " " + amount + " -> " + (ok ? "approved" : "declined"));
        return ok;
    }
}
