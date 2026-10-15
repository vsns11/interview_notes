package lld.ch16.meetingscheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** A half-open time range [start, end): includes start, excludes end, so 10:00-11:00 and 11:00-12:00 do not clash. */
record Interval(LocalDateTime start, LocalDateTime end) {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE MM-dd HH:mm");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    Interval {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if (!end.isAfter(start)) throw new IllegalArgumentException("end must be after start");
    }

    static Interval of(LocalDateTime start, int minutes) { return new Interval(start, start.plusMinutes(minutes)); }

    boolean overlaps(Interval o) { return start.isBefore(o.end) && o.start.isBefore(end); }

    Duration length() { return Duration.between(start, end); }

    Interval shiftedBy(Duration d) { return new Interval(start.plus(d), end.plus(d)); }

    @Override public String toString() { return DAY.format(start) + "-" + TIME.format(end); }
}

record User(String id, String name) {
    @Override public String toString() { return id; }
}

enum Equipment { WHITEBOARD, PROJECTOR, VIDEO_CONFERENCE }

/** An invitee's answer. Everyone starts PENDING; an answer can be changed until the meeting is cancelled. */
enum RsvpStatus { PENDING, ACCEPTED, TENTATIVE, DECLINED }

enum MeetingStatus { SCHEDULED, CANCELLED }

enum Frequency {
    DAILY(Duration.ofDays(1)), WEEKLY(Duration.ofDays(7));
    final Duration period;
    Frequency(Duration period) { this.period = period; }
}

/** Extension: "every week, 4 times". Expands one interval into its occurrences. */
record Recurrence(Frequency frequency, int count) {
    Recurrence {
        if (count < 1 || count > 52) throw new IllegalArgumentException("count must be 1..52");
    }

    List<Interval> occurrences(Interval first) {
        if (first.length().compareTo(frequency.period) >= 0)
            throw new IllegalArgumentException("a meeting cannot be longer than its repeat period");
        var out = new ArrayList<Interval>();
        for (int i = 0; i < count; i++) out.add(first.shiftedBy(frequency.period.multipliedBy(i)));
        return out;
    }
}

/**
 * What the organizer asks for. roomId == null means "pick a room for me" (the selection strategy decides).
 * needs = equipment the room must have.
 */
record MeetingRequest(User organizer, String title, Interval when, List<User> invitees, Set<Equipment> needs,
                      String roomId) {
    MeetingRequest {
        invitees = List.copyOf(invitees);
        needs = needs.isEmpty() ? Set.of() : Set.copyOf(needs);
        if (invitees.contains(organizer)) throw new IllegalArgumentException("organizer is not an invitee");
    }

    static MeetingRequest auto(User organizer, String title, Interval when, List<User> invitees, Set<Equipment> needs) {
        return new MeetingRequest(organizer, title, when, invitees, needs, null);
    }

    static MeetingRequest inRoom(User organizer, String title, Interval when, List<User> invitees, String roomId) {
        return new MeetingRequest(organizer, title, when, invitees, Set.of(), roomId);
    }

    int headcount() { return invitees.size() + 1; }
}

/** ok + the meeting(s) created, or a reason. warnings = invitees who are already busy (soft conflicts). */
record BookingResult(boolean ok, List<Meeting> meetings, String message, List<String> warnings) {
    static BookingResult success(List<Meeting> meetings, String message, List<String> warnings) {
        return new BookingResult(true, List.copyOf(meetings), message, List.copyOf(warnings));
    }

    static BookingResult failure(String why) { return new BookingResult(false, List.of(), why, List.of()); }

    Meeting meeting() { return meetings.get(0); }
}
