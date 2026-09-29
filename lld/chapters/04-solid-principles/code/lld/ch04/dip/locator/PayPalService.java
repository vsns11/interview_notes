package lld.ch04.dip.locator;

import java.math.BigDecimal;

public class PayPalService implements PaymentService {
    @Override
    public void processPayment(BigDecimal amount) {
        System.out.println("Processing payment via PayPal: $" + amount);
    }
}
