package lld.ch11.di;

import java.time.Clock;
import java.time.LocalTime;

/**
 * CONSTRUCTOR INJECTION: the service declares what it needs and never calls 'new'
 * on its collaborators. Even the clock is injected, so time is testable.
 */
public final class OrderService {
    private final MessageSender sender;
    private final Clock clock;

    public OrderService(MessageSender sender, Clock clock) {
        this.sender = sender;
        this.clock = clock;
    }

    public void placeOrder(String customer, String orderId) {
        LocalTime now = LocalTime.now(clock);
        sender.send(customer, "order " + orderId + " placed at " + now);
    }
}
