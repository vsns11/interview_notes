package lld.ch16.airline;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

interface Notifier { void send(String to, String message); }

final class EmailNotifier implements Notifier {
    public void send(String to, String message) { System.out.println("    [email " + to + "] " + message); }
}

/**
 * Facade over the airline's data: admin operations, search, booking, crew and cancellation.
 * Keeps an index flight-instance -> reservations so a cancelled flight can reach every passenger.
 */
final class Airline {
    private final String name;
    private final FareStrategy fares;
    private final Notifier notifier;
    private final Map<String, Aircraft> fleet = new ConcurrentHashMap<>();
    private final List<FlightInstance> instances = new CopyOnWriteArrayList<>();
    private final Map<FlightInstance, List<FlightReservation>> byInstance = new ConcurrentHashMap<>();
    private final Map<FlightReservation, Itinerary> owner = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(100);

    Airline(String name, FareStrategy fares, Notifier notifier) {
        this.name = name; this.fares = fares; this.notifier = notifier;
    }

    // ---- admin operations ----
    void addAircraft(Admin by, Aircraft a) { fleet.put(a.registration(), a); }

    List<FlightInstance> publish(Admin by, Flight f, LocalDate from, LocalDate toExclusive) {
        var created = f.generateInstances(from, toExclusive);
        instances.addAll(created);
        return created;
    }

    /** A crew member cannot be on two instances whose times overlap. */
    synchronized void assignCrew(Admin by, FlightInstance fi, CrewMember m) {
        for (var other : instances) {
            if (other != fi && other.crew().contains(m) && other.overlaps(fi))
                throw new IllegalStateException(m.name() + " already flies " + other.key() + " at that time");
        }
        if (fi.crew().contains(m)) throw new IllegalStateException(m.name() + " already on " + fi.key());
        fi.addCrew(m);
    }

    List<FlightInstance> scheduleFor(CrewMember m) {
        return instances.stream().filter(fi -> fi.crew().contains(m)).toList();
    }

    void cancelFlight(Admin by, FlightInstance fi) {
        List<FlightReservation> affected;
        synchronized (fi) {        // same lock as the hold+index step in book(): no booking can slip past the cancel
            fi.setStatus(FlightStatus.CANCELLED);
            affected = List.copyOf(byInstance.getOrDefault(fi, List.of()));
        }
        for (var r : affected) {
            if (r.cancel()) {
                var it = owner.get(r);
                notifier.send(it.customer().email(), fi.key() + " is cancelled. Reservation " + r.number()
                        + " refunded " + r.amount() + ". Itinerary " + it.id() + " now totals " + it.total());
            }
        }
        fi.crew().forEach(c -> notifier.send(c.id() + "@crew", "Stand down: " + fi.key() + " cancelled"));
    }

    // ---- customer operations ----
    List<FlightInstance> search(Airport from, Airport to, LocalDate date) {
        return instances.stream().filter(fi -> fi.flight().from().equals(from) && fi.flight().to().equals(to)
                && fi.departure().toLocalDate().equals(date) && fi.status() != FlightStatus.CANCELLED).toList();
    }

    Itinerary newItinerary(Customer c) { return new Itinerary("IT-" + seq.incrementAndGet(), c); }

    /** Books one leg for several passengers. Throws if the connection is invalid; returns null if a seat is taken. */
    FlightReservation book(Itinerary it, FlightInstance fi, Map<Passenger, String> seatRequest, LocalDate bookedOn) {
        it.checkConnection(fi);
        String resNo = "PNR" + seq.incrementAndGet();
        FlightReservation r;
        synchronized (fi) {        // hold the seats AND index the reservation as one step (see cancelFlight)
            var seatMap = fi.holdSeats(seatRequest, resNo);
            if (seatMap.isEmpty()) return null;
            var amount = seatMap.values().stream().map(s -> fares.price(fi, s, bookedOn)).reduce(BigDecimal.ZERO, BigDecimal::add);
            r = new FlightReservation(resNo, fi, seatMap, amount);
            owner.put(r, it);
            byInstance.computeIfAbsent(fi, k -> new CopyOnWriteArrayList<>()).add(r);
        }
        it.addLeg(r);
        var seatMap = r.seatMap();
        var amount = r.amount();
        notifier.send(it.customer().email(), "Booked " + fi.key() + " seats " + seatMap.values().stream().map(Seat::number).sorted().toList() + " (" + resNo + ", " + amount + ")");
        return r;
    }

    /** The front desk books for a walk-in customer: the same call, made by a different actor. */
    FlightReservation bookFor(FrontDeskOfficer by, Itinerary it, FlightInstance fi, Map<Passenger, String> seatRequest, LocalDate bookedOn) {
        return book(it, fi, seatRequest, bookedOn);
    }

    void cancelItinerary(Itinerary it) {
        it.legs().forEach(FlightReservation::cancel);
        notifier.send(it.customer().email(), "Itinerary " + it.id() + " cancelled");
    }

    String name() { return name; }
}
