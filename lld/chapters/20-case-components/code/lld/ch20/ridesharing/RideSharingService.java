package lld.ch20.ridesharing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Observer: rider/driver apps (push notifications) listen to trip events. */
interface TripListener {
    void onEvent(Trip trip, String event);
}

/** The class the apps talk to. Owns accounts and trips; holds the pricing and matching rules. */
final class RideSharingService {
    private final Map<String, Rider> riders = new ConcurrentHashMap<>();
    private final Map<String, Driver> drivers = new ConcurrentHashMap<>();
    private final Map<String, Trip> trips = new ConcurrentHashMap<>();
    private final List<TripListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger ids = new AtomicInteger();
    private volatile PricingStrategy pricing;
    private volatile MatchingStrategy matching;

    RideSharingService(PricingStrategy pricing, MatchingStrategy matching) {
        this.pricing = pricing;
        this.matching = matching;
    }

    void setPricing(PricingStrategy p) { this.pricing = p; }   // e.g. wrap in SurgePricing at rush hour
    void setMatching(MatchingStrategy m) { this.matching = m; }
    void addListener(TripListener l) { listeners.add(l); }
    void addRider(Rider r) { riders.put(r.id(), r); }
    void addDriver(Driver d) { drivers.put(d.id(), d); }
    Driver driver(String id) { return drivers.get(id); }
    Rider rider(String id) { return riders.get(id); }

    /** Rank candidates once, then offer to the first. No candidates -> canceled immediately. */
    Trip requestRide(String riderId, Location pickup, Location drop, VehicleType type) {
        List<Driver> ranked = matching.rank(pickup, type, drivers.values());
        Trip trip = new Trip("T" + ids.incrementAndGet(), riderId, pickup, drop, type, ranked);
        trips.put(trip.id(), trip);
        publish(trip, "requested by " + riderId + ", candidates " + ranked.stream().map(Driver::id).toList());
        offerOrCancel(trip, trip.currentCandidate());
        return trip;
    }

    /** Driver answers the offer. A lost race for the driver counts as a decline. */
    boolean respond(String tripId, String driverId, boolean accepts) {
        Trip trip = trips.get(tripId);
        Driver current = trip.currentCandidate();
        if (current == null || !current.id().equals(driverId) || trip.status() != TripStatus.REQUESTED) return false;
        if (accepts && current.tryClaim()) {
            if (trip.accept(driverId)) {
                publish(trip, "accepted by " + driverId);
                return true;
            }
            current.release(); // trip was canceled meanwhile
            return false;
        }
        publish(trip, (accepts ? "could not claim " : "declined by ") + driverId);
        offerOrCancel(trip, trip.nextCandidate());
        return false;
    }

    boolean startTrip(String tripId) {
        Trip trip = trips.get(tripId);
        boolean ok = trip.start();
        publish(trip, ok ? "started" : "start refused in state " + trip.status());
        return ok;
    }

    /** Price -> store -> charge -> free the driver -> move driver to drop point -> history. */
    boolean endTrip(String tripId, int minutes) {
        Trip trip = trips.get(tripId);
        BigDecimal fare = pricing.fare(trip.type(), trip.distanceKm(), minutes);
        if (!trip.end(minutes, fare)) {
            publish(trip, "end refused in state " + trip.status());
            return false;
        }
        Rider rider = riders.get(trip.riderId());
        Receipt receipt = rider.paymentMethod().charge(fare);
        Driver d = drivers.get(trip.driverId());
        d.moveTo(trip.drop());       // easy to forget: the car is now at the drop point
        d.release();
        rider.recordTrip(trip.id());
        d.recordTrip(trip.id());
        publish(trip, "ended: " + trip.distanceKm() + " km, " + minutes + " min, fare " + fare
                + " (" + receipt.detail() + ")");
        return true;
    }

    /** Rider may cancel before the trip starts. */
    boolean cancelTrip(String tripId) {
        Trip trip = trips.get(tripId);
        if (!trip.cancel()) {
            publish(trip, "cancel refused in state " + trip.status());
            return false;
        }
        String driverId = trip.driverId(); // read AFTER cancel: no accept can happen any more
        if (driverId != null) drivers.get(driverId).release();
        publish(trip, "canceled by rider");
        return true;
    }

    boolean rateDriver(String tripId, int stars) {
        Trip t = trips.get(tripId);
        if (!t.markDriverRated()) return false;
        drivers.get(t.driverId()).addRating(stars);
        return true;
    }

    boolean rateRider(String tripId, int stars) {
        Trip t = trips.get(tripId);
        if (!t.markRiderRated()) return false;
        riders.get(t.riderId()).addRating(stars);
        return true;
    }

    private void offerOrCancel(Trip trip, Driver next) {
        if (next != null) {
            publish(trip, "offered to " + next.id());
        } else if (trip.cancel()) {
            publish(trip, "no driver left - canceled");
        }
    }

    private void publish(Trip trip, String event) {
        for (TripListener l : listeners) l.onEvent(trip, event);
    }
}
