package ch18.shop;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The "app": wires real-looking collaborators and places two orders. Also the jar's main class (18.12). */
public class ShopDemo {
    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.WARNING);          // keep the demo quiet: only WARNING and above
        PaymentGateway gateway = (customer, amount) ->          // pretend processor: declines 500 and above
                new PaymentGateway.PaymentResult(amount.compareTo(new BigDecimal("500")) < 0, "PAY-" + customer);
        AtomicInteger seq = new AtomicInteger();
        OrderService service = new OrderService(gateway, new InMemoryOrderRepository(),
                Clock.fixed(Instant.parse("2025-09-16T10:00:00Z"), ZoneOffset.UTC),
                () -> "ORD-" + seq.incrementAndGet());

        Cart cart = new Cart();
        cart.add(new Item("BK-1", "Java book", new BigDecimal("45.00"), 2));
        cart.add(new Item("MUG", "Coffee mug", new BigDecimal("12.50"), 1));
        System.out.println("items=" + cart.count() + " subtotal=" + cart.subtotal());
        System.out.println(service.checkout("ann", cart, "SAVE25"));

        Cart big = new Cart();
        big.add(new Item("LAP", "Laptop", new BigDecimal("899.00"), 1));
        try {
            service.checkout("bob", big, null);
        } catch (PaymentDeclinedException e) {
            System.out.println("error: " + e.getMessage());
        }
    }
}
