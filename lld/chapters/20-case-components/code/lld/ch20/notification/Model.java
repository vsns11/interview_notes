package lld.ch20.notification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum ChannelType { EMAIL, SMS, PUSH }

enum Priority { NORMAL, URGENT }

enum DeliveryStatus { SENT, FAILED, SUPPRESSED }

/** Who a person is: identity and addresses. */
record User(String id, String name, String email, String phone, String deviceToken) {}

/** What a person is willing to receive, and when. Changes far more often than User. */
final class Preferences {
    private final Map<String, List<ChannelType>> channelsByKind = new HashMap<>();
    private Integer quietStart; // hour of day, inclusive
    private Integer quietEnd;   // hour of day, exclusive

    Preferences accept(String kind, ChannelType... inOrder) {
        channelsByKind.put(kind, List.of(inOrder));
        return this;
    }

    Preferences quietHours(int startHour, int endHour) {
        this.quietStart = startHour;
        this.quietEnd = endHour;
        return this;
    }

    List<ChannelType> channelsFor(String kind) { return channelsByKind.getOrDefault(kind, List.of()); }

    /** Quiet hours may wrap past midnight (22 -> 7), so the comparison lives here, once. */
    boolean isQuiet(int hour) {
        if (quietStart == null) return false;
        return quietStart <= quietEnd
                ? hour >= quietStart && hour < quietEnd
                : hour >= quietStart || hour < quietEnd;
    }
}

/** The finished wording handed to every channel. */
record Message(String subject, String body) {}

/** Wording for one kind of notification, with {holes} to fill in. */
record Template(String kind, String subject, String body) {
    private static final Pattern HOLE = Pattern.compile("\\{(\\w+)}");

    Message render(Map<String, String> values) {
        return new Message(fill(subject, values), fill(body, values));
    }

    private static String fill(String text, Map<String, String> values) {
        Matcher m = HOLE.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String v = values.get(m.group(1));
            if (v == null) throw new IllegalArgumentException("missing value for {" + m.group(1) + "}");
            m.appendReplacement(out, Matcher.quoteReplacement(v));
        }
        return m.appendTail(out).toString();
    }
}

/** One ask from product code. Values are kept sorted so the same request renders the same text. */
record NotificationRequest(String kind, String userId, Map<String, String> values, Priority priority) {
    NotificationRequest {
        values = new TreeMap<>(values);
    }
}

/** What happened to one notification. */
record DeliveryRecord(String kind, String userId, DeliveryStatus status, ChannelType channel,
                      int attempts, String reason) {}

/** Where "now" comes from, so quiet hours and rate limits are testable without waiting. */
interface TimeSource { LocalDateTime now(); }

final class ManualTime implements TimeSource {
    private LocalDateTime now;
    ManualTime(LocalDateTime start) { this.now = start; }
    public synchronized LocalDateTime now() { return now; }
    synchronized void set(LocalDateTime t) { now = t; }
}
