package lld.ch01.parkinglot;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Issues tickets and closes them. Time is PASSED IN (not read from the clock) so runs are
 * repeatable. Every method that reads or changes spots, tickets or pricing is synchronized on
 * the lot, so two entrance panels cannot hand out the same spot.
 */
public final class ParkingLot {
    private final List<ParkingFloor> floors;
    private final Map<String, Ticket> active = new HashMap<>();
    private PricingStrategy pricing;
    private int nextTicket = 1;

    public ParkingLot(List<ParkingFloor> floors, PricingStrategy pricing) {
        this.floors = List.copyOf(floors);
        this.pricing = pricing;
    }

    public synchronized Optional<Ticket> park(Vehicle v, long nowMinute) {
        for (ParkingFloor floor : floors) {
            Optional<ParkingSpot> spot = floor.findFreeSpot(v.type());
            if (spot.isPresent()) {
                spot.get().occupy(v);
                Ticket t = new Ticket("T" + nextTicket++, v, spot.get(), nowMinute);
                active.put(t.id(), t);
                return Optional.of(t);
            }
        }
        return Optional.empty();               // lot full for this vehicle type -> refuse entry
    }

    /** Closes the ticket, frees the spot and returns the fee. */
    public synchronized BigDecimal exit(String ticketId, long nowMinute) {
        Ticket t = active.get(ticketId);
        if (t == null) throw new IllegalArgumentException("unknown or already closed ticket " + ticketId);
        if (nowMinute < t.entryMinute()) throw new IllegalArgumentException("exit before entry");
        active.remove(ticketId);                // validate first, then change state
        t.spot().release();
        return pricing.fee(t.vehicle().type(), nowMinute - t.entryMinute());
    }

    /** Free spots of one type across all floors, read under the same lock as park/exit. */
    public synchronized long freeSpots(SpotType type) {
        return floors.stream().mapToLong(f -> f.freeCount(type)).sum();
    }

    public synchronized void setPricing(PricingStrategy pricing) { this.pricing = pricing; }
}
