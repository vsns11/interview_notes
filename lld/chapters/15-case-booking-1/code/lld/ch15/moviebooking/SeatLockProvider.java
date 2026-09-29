package lld.ch15.moviebooking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary, expiring holds on seats while the customer pays.
 * An interface so the in-memory version can be swapped for Redis (SET NX PX) or a DB row lock.
 */
interface SeatLockProvider {
    /** All-or-nothing: either every seat is locked for this user, or none is. */
    boolean lock(Show show, List<String> seatIds, String userId);
    /** Release this user's holds (payment failed, user gave up). */
    void unlock(Show show, List<String> seatIds, String userId);
    /** True while this user still holds every one of these seats and no hold has expired. */
    boolean holds(Show show, List<String> seatIds, String userId);
    /** Atomically: verify holds are still valid, mark seats BOOKED, drop the holds. */
    boolean commit(Show show, List<String> seatIds, String userId);
    /** Make booked seats sellable again (cancellation). */
    void releaseBooked(Show show, List<String> seatIds);
    Set<String> lockedSeats(Show show);
}

record SeatLock(String userId, Instant expiresAt) {
    boolean expired(Instant now) { return !now.isBefore(expiresAt); }
}

/**
 * One monitor per show: two shows never block each other, but every decision about one show's
 * seats (lock, commit, release) is made by one thread at a time. That is what makes
 * check-then-act safe and prevents double booking.
 */
final class InMemorySeatLockProvider implements SeatLockProvider {
    private final Duration ttl;
    private final Clock clock;
    private final Map<String, Map<String, SeatLock>> locksByShow = new ConcurrentHashMap<>();

    InMemorySeatLockProvider(Duration ttl, Clock clock) { this.ttl = ttl; this.clock = clock; }

    private Map<String, SeatLock> table(Show show) {
        return locksByShow.computeIfAbsent(show.id(), k -> new HashMap<>());
    }

    @Override public boolean lock(Show show, List<String> seatIds, String userId) {
        Map<String, SeatLock> t = table(show);
        synchronized (t) {
            Instant now = clock.instant();
            for (String id : seatIds) {                         // 1. check every seat first
                if (show.seat(id).isBooked()) return false;
                SeatLock l = t.get(id);
                if (l != null && !l.expired(now) && !l.userId().equals(userId)) return false;
            }
            SeatLock mine = new SeatLock(userId, now.plus(ttl));
            for (String id : seatIds) t.put(id, mine);           // 2. then take them all
            return true;
        }
    }

    @Override public void unlock(Show show, List<String> seatIds, String userId) {
        Map<String, SeatLock> t = table(show);
        synchronized (t) {
            for (String id : seatIds) {
                SeatLock l = t.get(id);
                if (l != null && l.userId().equals(userId)) t.remove(id);
            }
        }
    }

    @Override public boolean holds(Show show, List<String> seatIds, String userId) {
        Map<String, SeatLock> t = table(show);
        synchronized (t) { return holdsLocked(t, seatIds, userId, clock.instant()); }
    }

    @Override public boolean commit(Show show, List<String> seatIds, String userId) {
        Map<String, SeatLock> t = table(show);
        synchronized (t) {
            if (!holdsLocked(t, seatIds, userId, clock.instant())) return false;
            for (String id : seatIds) { show.seat(id).setBooked(true); t.remove(id); }
            return true;
        }
    }

    @Override public void releaseBooked(Show show, List<String> seatIds) {
        Map<String, SeatLock> t = table(show);
        synchronized (t) { for (String id : seatIds) show.seat(id).setBooked(false); }
    }

    @Override public Set<String> lockedSeats(Show show) {
        Map<String, SeatLock> t = table(show);
        synchronized (t) {
            Instant now = clock.instant();
            Set<String> out = new TreeSet<>();
            t.forEach((id, l) -> { if (!l.expired(now)) out.add(id); });
            return out;
        }
    }

    private static boolean holdsLocked(Map<String, SeatLock> t, List<String> ids, String userId, Instant now) {
        for (String id : ids) {
            SeatLock l = t.get(id);
            if (l == null || l.expired(now) || !l.userId().equals(userId)) return false;
        }
        return true;
    }
}
