package lld.ch20.notification;

import java.time.Duration;

/** Adapter target: the outside provider (SMTP server, SMS gateway, FCM/APNs). */
interface Gateway {
    /** Throws on failure. */
    void deliver(String address, Message message) throws Exception;
}

/** How many times to try and how long to wait between tries (exponential backoff). */
record RetryPolicy(int maxAttempts, Duration initialBackoff) {}

/** The outcome of one channel's delivery loop. */
record Attempt(boolean success, int attempts, String error) {}

/**
 * Template Method: deliver() is the fixed algorithm (try, back off, retry) and is final.
 * Subclasses fill in only the steps that differ: which address to use.
 */
abstract class NotificationChannel {
    private final Gateway gateway;
    private final RetryPolicy retry;

    NotificationChannel(Gateway gateway, RetryPolicy retry) {
        this.gateway = gateway;
        this.retry = retry;
    }

    abstract ChannelType type();

    /** Hook: where this channel reaches the user, or null if the user has no such address. */
    protected abstract String addressOf(User user);

    final Attempt deliver(User user, Message message) {
        String address = addressOf(user);
        if (address == null || address.isBlank()) return new Attempt(false, 0, "no " + type() + " address");
        String lastError = null;
        long backoff = retry.initialBackoff().toMillis();
        for (int attempt = 1; attempt <= retry.maxAttempts(); attempt++) {
            try {
                gateway.deliver(address, message);
                return new Attempt(true, attempt, null);
            } catch (Exception e) {
                lastError = e.getMessage();
                if (attempt < retry.maxAttempts()) sleep(backoff);
                backoff *= 2;
            }
        }
        return new Attempt(false, retry.maxAttempts(), lastError);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

final class EmailChannel extends NotificationChannel {
    EmailChannel(Gateway g, RetryPolicy r) { super(g, r); }
    ChannelType type() { return ChannelType.EMAIL; }
    protected String addressOf(User u) { return u.email(); }
}

final class SmsChannel extends NotificationChannel {
    SmsChannel(Gateway g, RetryPolicy r) { super(g, r); }
    ChannelType type() { return ChannelType.SMS; }
    protected String addressOf(User u) { return u.phone(); }
}

final class PushChannel extends NotificationChannel {
    PushChannel(Gateway g, RetryPolicy r) { super(g, r); }
    ChannelType type() { return ChannelType.PUSH; }
    protected String addressOf(User u) { return u.deviceToken(); }
}

/** Factory: the one place that knows which class implements which channel type. */
final class ChannelFactory {
    private ChannelFactory() {}

    static NotificationChannel create(ChannelType type, Gateway gateway, RetryPolicy retry) {
        return switch (type) {
            case EMAIL -> new EmailChannel(gateway, retry);
            case SMS -> new SmsChannel(gateway, retry);
            case PUSH -> new PushChannel(gateway, retry);
        };
    }
}
