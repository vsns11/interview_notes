package lld.ch16.meetingscheduler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One person's invitation to one meeting. Mutated only through its Meeting, under the meeting's lock. */
final class Invitation {
    private final User invitee;
    private RsvpStatus status = RsvpStatus.PENDING;

    Invitation(User invitee) { this.invitee = invitee; }

    User invitee() { return invitee; }
    RsvpStatus status() { return status; }
    void set(RsvpStatus s) { status = s; }

    @Override public String toString() { return invitee.id() + "=" + status; }
}

/**
 * A scheduled meeting: who, what, when, where, and the invitations. All methods are synchronized on the
 * meeting, so an RSVP, a reschedule and a cancel on the same meeting happen one at a time.
 * Lock order everywhere is meeting first, then room (the scheduler holds this monitor while it calls Room).
 */
final class Meeting {
    private final String id;
    private final String seriesId;           // null for a one-off meeting
    private final String title;
    private final User organizer;
    private final Room room;
    private Interval when;
    private MeetingStatus status = MeetingStatus.SCHEDULED;
    private final Map<String, Invitation> invitations = new LinkedHashMap<>();

    Meeting(String id, String seriesId, String title, User organizer, Room room, Interval when, List<User> invitees) {
        this.id = id; this.seriesId = seriesId; this.title = title;
        this.organizer = organizer; this.room = room; this.when = when;
        for (var u : invitees) invitations.put(u.id(), new Invitation(u));
    }

    String id() { return id; }
    String seriesId() { return seriesId; }
    String title() { return title; }
    User organizer() { return organizer; }
    Room room() { return room; }
    synchronized Interval when() { return when; }
    synchronized MeetingStatus status() { return status; }
    synchronized boolean isScheduled() { return status == MeetingStatus.SCHEDULED; }

    synchronized List<User> invitees() { return invitations.values().stream().map(Invitation::invitee).toList(); }

    synchronized boolean isInvited(String userId) { return invitations.containsKey(userId); }

    synchronized RsvpStatus rsvpOf(String userId) {
        var inv = invitations.get(userId);
        if (inv == null) throw new IllegalArgumentException(userId + " is not invited to " + id);
        return inv.status();
    }

    /** Is this person expected in the room? The organizer always; an invitee unless they declined. */
    synchronized boolean attends(String userId) {
        if (organizer.id().equals(userId)) return true;
        var inv = invitations.get(userId);
        return inv != null && inv.status() != RsvpStatus.DECLINED;
    }

    synchronized void respond(String userId, RsvpStatus answer) {
        if (status == MeetingStatus.CANCELLED) throw new IllegalStateException(id + " is cancelled");
        if (answer == RsvpStatus.PENDING) throw new IllegalArgumentException("cannot answer PENDING");
        var inv = invitations.get(userId);
        if (inv == null) throw new IllegalArgumentException(userId + " is not invited to " + id);
        inv.set(answer);
    }

    /** Called by the scheduler after the room calendar moved. A new time invalidates earlier answers. */
    synchronized void movedTo(Interval newWhen) {
        if (status == MeetingStatus.CANCELLED) throw new IllegalStateException(id + " is cancelled");
        when = newWhen;
        invitations.values().forEach(i -> i.set(RsvpStatus.PENDING));
    }

    /** Returns false if it was already cancelled (cancel is idempotent: doing it twice equals doing it once). */
    synchronized boolean markCancelled() {
        if (status == MeetingStatus.CANCELLED) return false;
        status = MeetingStatus.CANCELLED;
        return true;
    }

    synchronized String rsvpSummary() { return invitations.values().toString(); }

    @Override public synchronized String toString() {
        return id + " '" + title + "' " + when + " in " + room.name() + (status == MeetingStatus.CANCELLED ? " [CANCELLED]" : "");
    }
}
