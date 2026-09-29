package lld.ch04.checkout.step0;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** STEP 0 -- the class that grew quietly for two years. It works. */
public class OrderService {
    public void process(CustomerOrder order) {
        // 1. Validation  (business-rules owner)
        if (order.amount().signum() <= 0) {
            System.out.println("Rejected: amount must be positive");
            return;
        }
        // 2. Pricing, one branch per customer type  (finance)
        BigDecimal total = order.amount();
        if (order.customerType().equals("member")) {
            total = total.multiply(new BigDecimal("0.90"));
        } else if (order.customerType().equals("staff")) {
            total = total.multiply(new BigDecimal("0.70"));
        }
        total = total.setScale(2, RoundingMode.HALF_EVEN); // money: always 2 decimals
        // 3. Storage  (database team)
        System.out.println("INSERT INTO orders VALUES ('" + order.orderId() + "', " + total + ")");
        // 4. Telling the customer  (marketing)
        System.out.println("Emailing " + order.email() + ": your order total is " + total);
    }
}
