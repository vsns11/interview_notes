package lld.ch16.meetingscheduler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The service the organizer talks to. It never decides "is the room free?" itself: it asks each candidate
 * Room to claim the slot atomically and moves on to the next room if the claim fails.
 * Lock order: meeting monitor first, then the room lock (never the other way round), and never two rooms at once.
 */
final class MeetingScheduler {
    private final Map<String, Room> rooms = new ConcurrentSkipListMap<>();
    private final Map<String, Meeting> meetings = new ConcurrentHashMap<>();
    private final List<MeetingObserver> observers = new CopyOnWriteArrayList<>();
    private final AtomicLong ids = new AtomicLong();
    private volatile RoomSelectionStrategy strategy;

    MeetingScheduler(RoomSelectionStrategy strategy) { this.strategy = strategy; }

    void addRoom(Room r) { rooms.put(r.id(), r); }
    void subscribe(MeetingObserver o) { observers.add(o); }
    void useStrategy(RoomSelectionStrategy s) { strategy = s; }
    Meeting meeting(String id) { return meetings.get(id); }
    Room room(String id) { return rooms.get(id); }

    private void publish(EventType type, Meeting m, User to, String detail) {
        var e = new MeetingEvent(type, m, to, detail);
        observers.forEach(o -> o.onEvent(e));
    }

    /** Rooms to try, in order. An explicitly named room is the only candidate. */
    private List<Room> candidates(MeetingRequest r) {
        if (r.roomId() != null) {
            var room = rooms.get(r.roomId());
            if (room == null) throw new IllegalArgumentException("no room " + r.roomId());
            return room.fits(r.headcount(), r.needs()) ? List.of(room) : List.of();
        }
        var fitting = rooms.values().stream().filter(x -> x.fits(r.headcount(), r.needs())).toList();
        return strategy.rank(fitting, r);
    }

    BookingResult book(MeetingRequest r) {
        var rooms = candidates(r);
        if (rooms.isEmpty()) return BookingResult.failure("no room fits " + r.headcount() + " people with " + r.needs());
        String id = "M" + ids.incrementAndGet();
        for (var room : rooms) {
            if (!room.tryReserve(id, r.when())) continue;          // lost this room: try the next one
            var m = new Meeting(id, null, r.title(), r.organizer(), room, r.when(), r.invitees());
            meetings.put(id, m);
            for (var u : r.invitees()) publish(EventType.INVITED, m, u, "");
            return BookingResult.success(List.of(m), "booked " + m, busyPeople(m));
        }
        if (r.roomId() != null) {
            var blocker = rooms.get(0).conflictWith(r.when());
            return BookingResult.failure(r.roomId() + " is taken" + blocker.map(b -> " by " + b.meetingId() + " " + b.interval()).orElse(""));
        }
        return BookingResult.failure("all " + rooms.size() + " fitting rooms are taken at " + r.when());
    }

    /** Extension: a recurring series is all-or-nothing per room - every occurrence in the same room, or try the next room. */
    BookingResult bookRecurring(MeetingRequest r, Recurrence rule) {
        var rooms = candidates(r);
        if (rooms.isEmpty()) return BookingResult.failure("no room fits " + r.headcount() + " people with " + r.needs());
        String seriesId = "S" + ids.incrementAndGet();
        var slots = new LinkedHashMap<String, Interval>();
        for (var iv : rule.occurrences(r.when())) slots.put("M" + ids.incrementAndGet(), iv);
        for (var room : rooms) {
            if (!room.tryReserveAll(slots)) continue;
            var created = new ArrayList<Meeting>();
            slots.forEach((mid, iv) -> {
                var m = new Meeting(mid, seriesId, r.title(), r.organizer(), room, iv, r.invitees());
                meetings.put(mid, m);
                created.add(m);
            });
            String detail = " (series " + seriesId + ", " + rule.frequency() + " x" + rule.count() + ")";
            for (var u : r.invitees()) publish(EventType.INVITED, created.get(0), u, detail);
            return BookingResult.success(created, "booked series " + seriesId + " in " + room.name(), List.of());
        }
        return BookingResult.failure("no single fitting room is free for all " + rule.count() + " occurrences");
    }

    void respond(String meetingId, String userId, RsvpStatus answer) {
        var m = meetings.get(meetingId);
        m.respond(userId, answer);
        publish(EventType.RSVP, m, m.organizer(), userId + " " + answer);
    }

    BookingResult reschedule(String meetingId, String byUserId, Interval to) {
        var m = meetings.get(meetingId);
        Interval from;
        synchronized (m) {                                          // 1st lock: the meeting
            requireOrganizer(m, byUserId);
            if (!m.isScheduled()) return BookingResult.failure(meetingId + " is cancelled");
            from = m.when();
            if (!m.room().move(meetingId, from, to))                // 2nd lock: the room
                return BookingResult.failure(m.room().name() + " is not free at " + to);
            m.movedTo(to);
        }
        for (var u : m.invitees()) publish(EventType.RESCHEDULED, m, u, from + " -> " + to);
        return BookingResult.success(List.of(m), "moved " + m, busyPeople(m));
    }

    void cancel(String meetingId, String byUserId) {
        var m = meetings.get(meetingId);
        synchronized (m) {
            requireOrganizer(m, byUserId);
            if (!m.markCancelled()) return;                         // second cancel is a no-op
            m.room().release(meetingId, m.when());
        }
        for (var u : m.invitees()) publish(EventType.CANCELLED, m, u, "");
    }

    /** Cancels every occurrence of a series, one meeting at a time (one meeting lock at a time). */
    void cancelSeries(String seriesId, String byUserId) {
        meetings.values().stream().filter(m -> seriesId.equals(m.seriesId()))
                .sorted(Comparator.comparing(Meeting::when, Comparator.comparing(Interval::start)))
                .forEach(m -> cancel(m.id(), byUserId));
    }

    private static void requireOrganizer(Meeting m, String userId) {
        if (!m.organizer().id().equals(userId)) throw new SecurityException(userId + " is not the organizer of " + m.id());
    }

    /** Soft conflict: people who are expected somewhere else at the same time. Reported, not blocked. */
    List<String> busyPeople(Meeting m) {
        var people = new ArrayList<User>(m.invitees());
        people.add(m.organizer());
        var out = new ArrayList<String>();
        for (var u : people)
            for (var other : meetings.values())
                if (other != m && other.isScheduled() && other.attends(u.id()) && other.when().overlaps(m.when()))
                    out.add(u.id() + " is busy in " + other.id());
        return out;
    }

    /** A person's agenda for one day: scheduled meetings they organize or did not decline. */
    List<Meeting> agenda(String userId, LocalDate day) {
        return meetings.values().stream()
                .filter(m -> m.isScheduled() && m.attends(userId) && m.when().start().toLocalDate().equals(day))
                .sorted(Comparator.comparing(m -> m.when().start())).toList();
    }

    /** Advisory search: rooms that fit and look free right now (a later book() may still lose the race). */
    List<Room> freeRooms(Interval when, int people) {
        return rooms.values().stream().filter(r -> r.fits(people, java.util.Set.of()) && r.conflictWith(when).isEmpty()).toList();
    }
}
