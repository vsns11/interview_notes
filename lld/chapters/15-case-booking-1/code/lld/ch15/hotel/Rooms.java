package lld.ch15.hotel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

enum RoomStyle { STANDARD, DELUXE, FAMILY_SUITE, BUSINESS_SUITE }

/** What the room is doing RIGHT NOW. It cannot answer "is it free for 3 nights from the 10th?". */
enum RoomStatus { AVAILABLE, RESERVED, OCCUPIED, NOT_AVAILABLE, BEING_SERVICED, OTHER }

/** An electronic key card. Deactivated at checkout so it stops opening the door. */
final class RoomKey {
    private final String keyId, barcode;
    private final LocalDateTime issuedAt;
    private final boolean master;
    private volatile boolean active = true;
    RoomKey(String keyId, String barcode, LocalDateTime issuedAt, boolean master) {
        this.keyId = keyId; this.barcode = barcode; this.issuedAt = issuedAt; this.master = master;
    }
    String keyId()        { return keyId; }
    boolean isActive()    { return active; }
    boolean isMaster()    { return master; }
    void deactivate()     { active = false; }
}

/** Append-only log line: nothing is overwritten, so a room's history can be read back. */
record RoomHouseKeeping(LocalDateTime at, String housekeeper, String description, int minutes) {}

/**
 * A room owns its keys and its housekeeping log (composition), and the SET OF NIGHTS it is committed to.
 * Reserving nights is check-then-act, so it runs under the room's monitor: two guests can never
 * both get the night of the 10th.
 */
final class Room {
    private final String number;
    private final RoomStyle style;
    private final BigDecimal pricePerNight;
    private final boolean smoking;
    private final Set<LocalDate> bookedNights = new HashSet<>();
    private final List<RoomKey> keys = new ArrayList<>();
    private final List<RoomHouseKeeping> housekeeping = new ArrayList<>();
    private RoomStatus status = RoomStatus.AVAILABLE;

    Room(String number, RoomStyle style, BigDecimal pricePerNight, boolean smoking) {
        this.number = number; this.style = style; this.pricePerNight = pricePerNight; this.smoking = smoking;
    }
    String number()             { return number; }
    RoomStyle style()           { return style; }
    BigDecimal pricePerNight()  { return pricePerNight; }

    /** Nights are [checkIn, checkOut): the checkout day itself is free for the next guest. */
    synchronized boolean isFree(LocalDate checkIn, LocalDate checkOut) {
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) if (bookedNights.contains(d)) return false;
        return true;
    }
    synchronized boolean tryReserve(LocalDate checkIn, LocalDate checkOut) {
        if (!isFree(checkIn, checkOut)) return false;              // re-entrant monitor: same lock
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) bookedNights.add(d);
        return true;
    }
    synchronized void release(LocalDate checkIn, LocalDate checkOut) {
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) bookedNights.remove(d);
    }

    synchronized RoomStatus status()          { return status; }
    synchronized void setStatus(RoomStatus s) { status = s; }

    synchronized RoomKey issueKey(String keyId, LocalDateTime at) {
        RoomKey k = new RoomKey(keyId, "BC-" + keyId, at, false);
        keys.add(k);
        return k;
    }
    synchronized int deactivateGuestKeys() {
        int n = 0;
        for (RoomKey k : keys) if (!k.isMaster() && k.isActive()) { k.deactivate(); n++; }
        return n;
    }
    synchronized void addHousekeeping(RoomHouseKeeping h) { housekeeping.add(h); }
    synchronized List<RoomHouseKeeping> housekeepingLog()  { return List.copyOf(housekeeping); }
}

/** Search belongs to the LOCATION: a single room only knows about itself. */
interface Search {
    List<Room> search(RoomStyle style, LocalDate checkIn, LocalDate checkOut);
}

final class HotelLocation implements Search {
    private final String name;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    HotelLocation(String name) { this.name = name; }
    String name()           { return name; }
    void addRoom(Room r)    { rooms.put(r.number(), r); }
    Room room(String n)     { return rooms.get(n); }

    @Override public List<Room> search(RoomStyle style, LocalDate in, LocalDate out) {
        return rooms.values().stream().filter(r -> r.style() == style && r.isFree(in, out))
                .sorted(Comparator.comparing(Room::number)).toList();
    }
}

final class Hotel {
    private final String name;
    private final List<HotelLocation> locations = new ArrayList<>();
    Hotel(String name) { this.name = name; }
    void addLocation(HotelLocation l) { locations.add(l); }
    /** Asks every location in turn. */
    Map<String, List<Room>> search(RoomStyle style, LocalDate checkIn, LocalDate checkOut) {
        Map<String, List<Room>> result = new TreeMap<>();
        for (HotelLocation l : locations) result.put(l.name(), l.search(style, checkIn, checkOut));
        return result;
    }
}

final class SimClock extends Clock {
    private volatile Instant now;
    SimClock(Instant start) { this.now = start; }
    void advance(Duration d) { now = now.plus(d); }
    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return now; }
}

final class Money {
    private Money() {}
    static String fmt(BigDecimal amount) {
        String digits = amount.abs().setScale(2, RoundingMode.UNNECESSARY).toPlainString();
        return (amount.signum() < 0 ? "-$" : "$") + digits;
    }
}
