package lld.ch09.bridge;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Channel email = new EmailChannel("ops@shop.com");
        Channel sms = new SmsChannel("+1-555-0100");
        Channel push = new PushChannel("pixel-7");

        System.out.println("== Any type x any channel, paired at run time ==");
        List<Notification> outbox = List.of(
                new AlertNotification(email, "payments", "error rate 12%"),
                new AlertNotification(sms, "payments", "error rate 12%"),
                new ReminderNotification(push, "Stand-up", "09:30"),
                new DigestNotification(email, List.of("3 orders", "1 refund", "0 incidents")),
                new DigestNotification(sms, List.of("3 orders", "1 refund", "0 incidents")));
        outbox.forEach(Notification::send);

        System.out.println("== Class count ==");
        int types = 3, channels = 3;
        System.out.println("  one hierarchy (type x channel): " + types * channels + " classes");
        System.out.println("  bridge (type + channel):        " + (types + channels) + " classes");
        System.out.println("  with 4 types and 4 channels:    " + 4 * 4 + " vs " + (4 + 4));
    }
}
