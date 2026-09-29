package lld.ch04.checkout.step1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lld.ch04.checkout.step0.CustomerOrder;

/** Actor: finance. SRP is satisfied, but the branch is still here -> OCP problem next. */
public class PriceCalculator {
    public BigDecimal total(CustomerOrder order) {
        BigDecimal total = switch (order.customerType()) {
            case "member" -> order.amount().multiply(new BigDecimal("0.90"));
            case "staff" -> order.amount().multiply(new BigDecimal("0.70"));
            default -> order.amount();
        };
        return total.setScale(2, RoundingMode.HALF_EVEN);
    }
}
