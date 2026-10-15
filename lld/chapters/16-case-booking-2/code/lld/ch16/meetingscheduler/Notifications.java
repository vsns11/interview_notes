package lld.ch16.meetingscheduler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

enum EventType { INVITED, RSVP, RESCHEDULED, CANCELLED }

/** Something happened to a meeting that one person should hear about. */
record MeetingEvent(EventType type, Meeting meeting, User recipient, String detail) {}

/** Observer: the scheduler publishes events; it does not know who listens or how messages travel. */
interface MeetingObserver { void onEvent(MeetingEvent e); }

/** Keeps every person's messages (stands in for e-mail / push). Can echo to the console for the demo. */
final class InboxNotifier implements MeetingObserver {
    private final Map<String, List<String>> inboxes = new ConcurrentHashMap<>();
    private volatile boolean echo = true;

    void echo(boolean on) { echo = on; }

    @Override public void onEvent(MeetingEvent e) {
        String text = switch (e.type()) {
            case INVITED -> e.meeting().organizer() + " invited you to " + e.meeting() + e.detail();
            case RSVP -> e.detail() + " for " + e.meeting().id();
            case RESCHEDULED -> e.meeting().id() + " moved: " + e.detail() + " - please RSVP again";
            case CANCELLED -> e.meeting().id() + " '" + e.meeting().title() + "' was cancelled" + e.detail();
        };
        inboxes.computeIfAbsent(e.recipient().id(), k -> java.util.Collections.synchronizedList(new ArrayList<>())).add(text);
        if (echo) System.out.println("    [to " + e.recipient() + "] " + text);
    }

    List<String> inbox(String userId) {
        var box = inboxes.get(userId);
        if (box == null) return List.of();
        synchronized (box) { return List.copyOf(box); }
    }
}

/** A second, unrelated observer: counts events by type (a stand-in for metrics or an audit log). */
final class EventCounter implements MeetingObserver {
    private final Map<EventType, AtomicInteger> counts = new EnumMap<>(EventType.class);

    EventCounter() { for (var t : EventType.values()) counts.put(t, new AtomicInteger()); }

    @Override public void onEvent(MeetingEvent e) { counts.get(e.type()).incrementAndGet(); }

    @Override public String toString() { return counts.toString(); }
}
