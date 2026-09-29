package lld.ch08.simplefactory;

/** The product interface every caller programs against. */
public interface Notification {
    String send(String to, String message);
}

record EmailNotification() implements Notification {
    public String send(String to, String message) { return "EMAIL to " + to + ": " + message; }
}

record SmsNotification() implements Notification {
    public String send(String to, String message) { return "SMS to " + to + ": " + message; }
}

record PushNotification() implements Notification {
    public String send(String to, String message) { return "PUSH to " + to + ": " + message; }
}
