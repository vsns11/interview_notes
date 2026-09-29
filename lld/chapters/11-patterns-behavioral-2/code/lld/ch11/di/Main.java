package lld.ch11.di;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class Main {
    public static void main(String[] args) {
        Clock fixed = Clock.fixed(Instant.parse("2026-01-15T09:30:00Z"), ZoneOffset.UTC);

        System.out.println("=== 1. Manual DI: the composition root wires everything ===");
        new OrderService(new EmailSender(), fixed).placeOrder("asha@example.com", "A-1");
        new OrderService(new SmsSender(), fixed).placeOrder("+91-90000-00000", "A-2");

        System.out.println("\n=== 2. In a unit test: inject a fake ===");
        RecordingSender fake = new RecordingSender();
        new OrderService(fake, fixed).placeOrder("ben", "T-9");
        System.out.println("  fake recorded: " + fake.sent);

        System.out.println("\n=== 3. A toy IoC container ===");
        Container c = new Container();
        c.register(Clock.class, k -> fixed);
        c.register(MessageSender.class, k -> new EmailSender());
        c.register(OrderService.class, k -> new OrderService(k.get(MessageSender.class), k.get(Clock.class)));
        c.get(OrderService.class).placeOrder("chen@example.com", "A-3");
        System.out.println("  same instance each time: " + (c.get(OrderService.class) == c.get(OrderService.class)));
    }
}
