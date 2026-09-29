package lld.ch20.notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    /** A fake provider that fails the next N calls, then succeeds, printing what it "sent". */
    static final class FakeGateway implements Gateway {
        private final String name;
        private final AtomicInteger failuresLeft = new AtomicInteger();
        FakeGateway(String name) { this.name = name; }
        void failNext(int n) { failuresLeft.set(n); }

        @Override
        public void deliver(String address, Message m) throws Exception {
            if (failuresLeft.getAndUpdate(x -> Math.max(0, x - 1)) > 0) throw new Exception(name + " timeout");
            System.out.println("    " + name + " -> " + address + " : " + m.subject() + " | " + m.body());
        }
    }

    public static void main(String[] args) throws Exception {
        FakeGateway smtp = new FakeGateway("SMTP"), twilio = new FakeGateway("SMS-GW"), fcm = new FakeGateway("FCM");
        RetryPolicy retry = new RetryPolicy(3, Duration.ofMillis(1));
        ManualTime clock = new ManualTime(LocalDateTime.of(2026, 9, 28, 14, 0));

        MetricsListener metrics = new MetricsListener();
        try (NotificationService svc = new NotificationService(
                List.of(ChannelFactory.create(ChannelType.EMAIL, smtp, retry),
                        ChannelFactory.create(ChannelType.SMS, twilio, retry),
                        ChannelFactory.create(ChannelType.PUSH, fcm, retry)),
                List.of(new QuietHoursRule(), new RateLimitRule(2, Duration.ofHours(1))),
                clock)) {
            svc.subscribe(new LoggingListener());
            svc.subscribe(metrics);

            svc.addTemplate(new Template("ORDER_SHIPPED", "Order {orderId} shipped", "Hi {name}, it arrives {eta}."));
            svc.addTemplate(new Template("OTP", "Your code", "{code} is your login code."));
            svc.addTemplate(new Template("PROMO", "Sale!", "{name}, {pct}% off today."));

            svc.register(new User("u1", "Asha", "asha@mail.com", "+91-98450", "tok-asha"),
                    new Preferences().accept("ORDER_SHIPPED", ChannelType.SMS, ChannelType.EMAIL)
                            .accept("OTP", ChannelType.PUSH, ChannelType.SMS)
                            .accept("PROMO", ChannelType.EMAIL)
                            .quietHours(22, 7));
            svc.register(new User("u2", "Ben", "ben@mail.com", null, "tok-ben"),
                    new Preferences().accept("ORDER_SHIPPED", ChannelType.PUSH));

            Map<String, String> order = Map.of("orderId", "A-17", "name", "Asha", "eta", "Tuesday");

            System.out.println("== 1. Normal send over the first accepted channel (SMS)");
            svc.send(new NotificationRequest("ORDER_SHIPPED", "u1", order, Priority.NORMAL));

            System.out.println("== 2. SMS fails 3 times -> retried, then falls back to EMAIL");
            twilio.failNext(3);
            svc.send(new NotificationRequest("ORDER_SHIPPED", "u1", order, Priority.NORMAL));

            System.out.println("== 3. Quiet hours at 23:00: normal is suppressed, urgent goes through");
            clock.set(LocalDateTime.of(2026, 9, 28, 23, 0));
            svc.send(new NotificationRequest("PROMO", "u1", Map.of("name", "Asha", "pct", "20"), Priority.NORMAL));
            svc.send(new NotificationRequest("OTP", "u1", Map.of("code", "482913"), Priority.URGENT));

            System.out.println("== 4. Rate limit: 2 PROMO per hour, the 3rd is suppressed");
            clock.set(LocalDateTime.of(2026, 9, 29, 10, 0));
            for (int i = 0; i < 3; i++)
                svc.send(new NotificationRequest("PROMO", "u1", Map.of("name", "Asha", "pct", "" + (10 + i)), Priority.NORMAL));
            clock.set(LocalDateTime.of(2026, 9, 29, 11, 1));
            System.out.println("  ...61 minutes later the window has moved:");
            svc.send(new NotificationRequest("PROMO", "u1", Map.of("name", "Asha", "pct", "50"), Priority.NORMAL));

            System.out.println("== 5. Every channel gives up -> FAILED");
            fcm.failNext(3);
            svc.send(new NotificationRequest("ORDER_SHIPPED", "u2",
                    Map.of("orderId", "B-2", "name", "Ben", "eta", "Friday"), Priority.NORMAL));

            System.out.println("== 6. No accepted channel for this kind -> SUPPRESSED");
            svc.send(new NotificationRequest("PROMO", "u2", Map.of("name", "Ben", "pct", "5"), Priority.NORMAL));

            System.out.println("== 7. Template value missing -> FAILED, still recorded through finish()");
            svc.send(new NotificationRequest("OTP", "u1", Map.of(), Priority.URGENT));

            System.out.println("== 8. Async send (caller not blocked)");
            DeliveryRecord r = svc.sendAsync(new NotificationRequest("ORDER_SHIPPED", "u2",
                    Map.of("orderId", "B-3", "name", "Ben", "eta", "Monday"), Priority.NORMAL)).get();
            System.out.println("  future completed with " + r.status() + " via " + r.channel());

            System.out.println("== Metrics: " + metrics.counts() + ", history size = " + svc.history().size());
        }
    }
}
