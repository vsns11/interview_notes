package lld.ch20.ridesharing;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The trip lifecycle as a table of legal moves (a compact State machine). */
enum TripStatus {
    REQUESTED, ACCEPTED, STARTED, ENDED, CANCELED;

    private static final Map<TripStatus, Set<TripStatus>> NEXT = Map.of(
            REQUESTED, EnumSet.of(ACCEPTED, CANCELED),
            ACCEPTED, EnumSet.of(STARTED, CANCELED),
            STARTED, EnumSet.of(ENDED),
            ENDED, EnumSet.noneOf(TripStatus.class),
            CANCELED, EnumSet.noneOf(TripStatus.class));

    boolean canMoveTo(TripStatus next) { return NEXT.get(this).contains(next); }
}

/** One journey. Every action checks the status first; an illegal action returns false and changes nothing. */
final class Trip {
    private final String id;
    private final String riderId;
    private final Location pickup;
    private final Location drop;
    private final VehicleType type;
    private final List<Driver> candidates; // ranked once; a decline just moves the cursor
    private int cursor;
    private TripStatus status = TripStatus.REQUESTED;
    private String driverId;
    private int minutes;
    private BigDecimal fare;
    private boolean riderRated, driverRated;

    Trip(String id, String riderId, Location pickup, Location drop, VehicleType type, List<Driver> candidates) {
        this.id = id;
        this.riderId = riderId;
        this.pickup = pickup;
        this.drop = drop;
        this.type = type;
        this.candidates = List.copyOf(candidates);
    }

    private boolean move(TripStatus next) {
        if (!status.canMoveTo(next)) return false;
        status = next;
        return true;
    }

    /** The driver currently being offered this trip, or null if the list is exhausted. */
    synchronized Driver currentCandidate() { return cursor < candidates.size() ? candidates.get(cursor) : null; }
    synchronized Driver nextCandidate() { cursor++; return currentCandidate(); }

    synchronized boolean accept(String driverId) {
        if (status != TripStatus.REQUESTED) return false;
        this.driverId = driverId;
        return move(TripStatus.ACCEPTED);
    }

    synchronized boolean start() { return move(TripStatus.STARTED); }

    synchronized boolean end(int minutes, BigDecimal fare) {
        if (!move(TripStatus.ENDED)) return false;
        this.minutes = minutes;
        this.fare = fare;
        return true;
    }

    synchronized boolean cancel() { return move(TripStatus.CANCELED); }

    synchronized boolean markRiderRated() { if (status != TripStatus.ENDED || riderRated) return false; return riderRated = true; }
    synchronized boolean markDriverRated() { if (status != TripStatus.ENDED || driverRated) return false; return driverRated = true; }

    String id() { return id; }
    String riderId() { return riderId; }
    Location pickup() { return pickup; }
    Location drop() { return drop; }
    VehicleType type() { return type; }
    int distanceKm() { return pickup.distanceTo(drop); }
    synchronized TripStatus status() { return status; }
    synchronized String driverId() { return driverId; }
    synchronized int minutes() { return minutes; }
    synchronized BigDecimal fare() { return fare; }
}
