package com.example.ioc.injection;

import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Three implementations of the same interface. */
public final class Notifiers {
    private Notifiers() {}

    @Component("sms") @Primary @Order(1)
    public static class SmsNotifier implements Notifier {
        public String send(String m) { return "SMS: " + m; }
    }

    @Component("email") @Order(2)
    public static class EmailNotifier implements Notifier {
        public String send(String m) { return "EMAIL: " + m; }
    }

    @Component("push") @Order(3)
    public static class PushNotifier implements Notifier {
        public String send(String m) { return "PUSH: " + m; }
    }
}
