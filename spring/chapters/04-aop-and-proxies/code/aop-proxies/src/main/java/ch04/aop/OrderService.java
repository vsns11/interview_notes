package ch04.aop;

import org.springframework.stereotype.Service;

/** Plain business code: no logging, no timing. The aspects add those. */
@Service
public class OrderService {

    public String placeOrder(String item, int quantity) {
        return "order-" + item + "-x" + quantity;
    }

    public void cancel(String orderId) {
        if (orderId.startsWith("unknown")) {
            throw new IllegalArgumentException("no such order: " + orderId);
        }
    }

    @Timed("monthly-report")
    public String monthlyReport() throws InterruptedException {
        Thread.sleep(20);                  // pretend to do slow work
        return "report ready";
    }
}
