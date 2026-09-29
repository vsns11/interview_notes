package lld.ch16.restaurant;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * status answers "what is this table doing NOW" (for walk-ins).
 * heldSlots answers "is it free at 19:00 on Friday" (for reservations).
 */
final class Table {
    static final int SLOTS_PER_BOOKING = 2;   // a booking holds the table for two hourly slots

    private final String id;
    private final int capacity;
    private final List<TableSeat> seats = new ArrayList<>();
    private final Set<LocalDateTime> heldSlots = new HashSet<>();
    private TableStatus status = TableStatus.FREE;

    Table(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        for (int i = 1; i <= capacity; i++) seats.add(new TableSeat(i, SeatType.REGULAR));
    }

    String id() { return id; }
    int capacity() { return capacity; }
    List<TableSeat> seats() { return List.copyOf(seats); }
    synchronized TableStatus status() { return status; }
    synchronized void setStatus(TableStatus s) { status = s; }

    private static List<LocalDateTime> slots(LocalDateTime start) {
        var first = start.truncatedTo(ChronoUnit.HOURS);
        List<LocalDateTime> out = new ArrayList<>();
        for (int i = 0; i < SLOTS_PER_BOOKING; i++) out.add(first.plusHours(i));
        return out;
    }

    synchronized boolean isFreeAt(LocalDateTime start) { return slots(start).stream().noneMatch(heldSlots::contains); }

    /** Atomic check-then-hold. */
    synchronized boolean tryHold(LocalDateTime start) {
        if (!isFreeAt(start)) return false;
        heldSlots.addAll(slots(start));
        return true;
    }

    synchronized void release(LocalDateTime start) { slots(start).forEach(heldSlots::remove); }
}

/** A named seating arrangement. It REFERS to tables (association); a table can appear in several charts. */
record TableChart(String name, List<Table> tables) {}
