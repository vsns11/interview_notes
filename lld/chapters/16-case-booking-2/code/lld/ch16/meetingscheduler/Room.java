package lld.ch16.meetingscheduler;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A meeting room and its calendar. The calendar is the ONLY place a time slot is claimed, and every
 * claim is a check-then-insert done under the room's own lock, so two organizers can never both win
 * the same slot. Different rooms have different locks, so they never block each other.
 */
final class Room {
    /** One entry in the room's calendar. */
    record Reservation(String meetingId, Interval interval) {}

    private final String id;
    private final String name;
    private final int capacity;
    private final Set<Equipment> equipment;
    /** Bookings keyed by start time. They never overlap, which makes the conflict check one lookup. */
    private final NavigableMap<LocalDateTime, Reservation> calendar = new TreeMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    Room(String id, String name, int capacity, Set<Equipment> equipment) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be positive");
        this.id = id; this.name = name; this.capacity = capacity;
        this.equipment = equipment.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(equipment));
    }

    String id() { return id; }
    String name() { return name; }
    int capacity() { return capacity; }
    Set<Equipment> equipment() { return equipment; }

    /** Static fit: enough seats and every required piece of equipment. Does not look at the calendar. */
    boolean fits(int people, Set<Equipment> needs) { return people <= capacity && equipment.containsAll(needs); }

    /**
     * The booking with the latest start before iv.end is the only one that can overlap iv: bookings are
     * disjoint and sorted, so if that one ends at or before iv.start, every earlier one does too. O(log n).
     */
    private Optional<Reservation> clash(Interval iv) {
        var e = calendar.lowerEntry(iv.end());
        return e != null && e.getValue().interval().end().isAfter(iv.start()) ? Optional.of(e.getValue()) : Optional.empty();
    }

    /** Atomic check-then-claim. */
    boolean tryReserve(String meetingId, Interval iv) {
        lock.lock();
        try {
            if (clash(iv).isPresent()) return false;
            calendar.put(iv.start(), new Reservation(meetingId, iv));
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** All-or-nothing claim of several slots (a recurring series): check every one, then insert every one. */
    boolean tryReserveAll(Map<String, Interval> byMeetingId) {
        lock.lock();
        try {
            for (var iv : byMeetingId.values()) if (clash(iv).isPresent()) return false;
            byMeetingId.forEach((mid, iv) -> calendar.put(iv.start(), new Reservation(mid, iv)));
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Moves a booking inside this room atomically: the old slot is restored if the new one is taken. */
    boolean move(String meetingId, Interval from, Interval to) {
        lock.lock();
        try {
            var old = calendar.get(from.start());
            if (old == null || !old.meetingId().equals(meetingId)) throw new IllegalStateException(meetingId + " not booked at " + from);
            calendar.remove(from.start());
            if (clash(to).isPresent()) {
                calendar.put(from.start(), old);
                return false;
            }
            calendar.put(to.start(), new Reservation(meetingId, to));
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Frees a slot; only the meeting that owns it can free it. */
    void release(String meetingId, Interval iv) {
        lock.lock();
        try {
            var r = calendar.get(iv.start());
            if (r != null && r.meetingId().equals(meetingId)) calendar.remove(iv.start());
        } finally {
            lock.unlock();
        }
    }

    /** Advisory read for search and error messages; the answer may be stale a moment later. */
    Optional<Reservation> conflictWith(Interval iv) {
        lock.lock();
        try { return clash(iv); } finally { lock.unlock(); }
    }

    List<Reservation> bookings() {
        lock.lock();
        try { return List.copyOf(calendar.values()); } finally { lock.unlock(); }
    }

    int bookingsOn(java.time.LocalDate day) {
        lock.lock();
        try {
            return (int) calendar.values().stream().filter(r -> r.interval().start().toLocalDate().equals(day)).count();
        } finally {
            lock.unlock();
        }
    }

    @Override public String toString() { return name + "(" + capacity + ")"; }
}
