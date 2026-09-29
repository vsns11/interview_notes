package lld.ch08.simplefactory;

public class Main {
    public static void main(String[] args) {
        System.out.println("== Simple Factory with an enum switch ==");
        for (NotificationFactory.Channel c : NotificationFactory.Channel.values()) {
            Notification n = NotificationFactory.create(c);      // caller never writes "new XxxNotification"
            System.out.println("  " + n.send("ada", "Your order shipped"));
        }

        System.out.println("\n== Simple Factory with a registry (string keys from config) ==");
        System.out.println("  " + NotificationFactory.create("SMS").send("+91-98xxxx", "OTP 4821"));
        try {
            NotificationFactory.create("whatsapp");
        } catch (IllegalArgumentException e) {
            System.out.println("  rejected: " + e.getMessage());
        }
        // A new channel is registered at start-up - the factory's code is not edited.
        NotificationFactory.register("whatsapp", () -> (to, msg) -> "WHATSAPP to " + to + ": " + msg);
        System.out.println("  " + NotificationFactory.create("whatsapp").send("grace", "Table for 2 confirmed"));
    }
}
