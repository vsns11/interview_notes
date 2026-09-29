package lld.ch16.airline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Strategy: what one seat on one flight instance costs, booked on a given day. */
interface FareStrategy { BigDecimal price(FlightInstance fi, Seat seat, LocalDate bookedOn); }

final class ClassMultiplierFare implements FareStrategy {
    private static final Map<SeatClass, BigDecimal> MULTIPLIER = new EnumMap<>(Map.of(
            SeatClass.ECONOMY, new BigDecimal("1.0"), SeatClass.ECONOMY_PLUS, new BigDecimal("1.3"),
            SeatClass.PREFERRED_ECONOMY, new BigDecimal("1.5"), SeatClass.BUSINESS, new BigDecimal("2.5"),
            SeatClass.FIRST_CLASS, new BigDecimal("4.0")));
    public BigDecimal price(FlightInstance fi, Seat seat, LocalDate bookedOn) {
        return fi.flight().baseFare().multiply(MULTIPLIER.get(seat.seatClass())).setScale(2, RoundingMode.HALF_UP);
    }
}

/** Decorator on any fare: +25% when booked less than 3 days before departure. */
final class LastMinuteFare implements FareStrategy {
    private final FareStrategy inner;
    LastMinuteFare(FareStrategy inner) { this.inner = inner; }
    public BigDecimal price(FlightInstance fi, Seat seat, LocalDate bookedOn) {
        var p = inner.price(fi, seat, bookedOn);
        long daysBefore = ChronoUnit.DAYS.between(bookedOn, fi.departure().toLocalDate());
        return daysBefore < 3 ? p.multiply(new BigDecimal("1.25")).setScale(2, RoundingMode.HALF_UP) : p;
    }
}

/** One leg: one flight instance, several passengers, each with a seat. */
final class FlightReservation {
    private final String number;
    private final FlightInstance instance;
    private final Map<Passenger, Seat> seatMap;
    private final BigDecimal amount;
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    FlightReservation(String number, FlightInstance instance, Map<Passenger, Seat> seatMap, BigDecimal amount) {
        this.number = number; this.instance = instance; this.seatMap = Map.copyOf(seatMap); this.amount = amount;
    }
    String number() { return number; }
    FlightInstance instance() { return instance; }
    Map<Passenger, Seat> seatMap() { return seatMap; }
    BigDecimal amount() { return amount; }
    synchronized ReservationStatus status() { return status; }

    /** Returns true if this call did the cancelling (idempotent). */
    synchronized boolean cancel() {
        if (status == ReservationStatus.CANCELLED) return false;
        status = ReservationStatus.CANCELLED;
        instance.releaseSeats(number);
        return true;
    }
}

/** A trip of one or more legs. The itinerary, not each leg, is what gets paid for. */
final class Itinerary {
    static final Duration MIN_CONNECTION = Duration.ofMinutes(45);

    private final String id;
    private final Customer customer;
    private final List<FlightReservation> legs = new ArrayList<>();
    private boolean paid;

    Itinerary(String id, Customer customer) { this.id = id; this.customer = customer; }
    String id() { return id; }
    Customer customer() { return customer; }
    synchronized List<FlightReservation> legs() { return List.copyOf(legs); }

    /** Rule the itinerary owns: the next leg must leave from where the last one landed, with time to change. */
    synchronized void checkConnection(FlightInstance next) {
        if (legs.isEmpty()) return;
        var last = legs.getLast().instance();
        if (!last.flight().to().equals(next.flight().from()))
            throw new IllegalArgumentException("leg must depart from " + last.flight().to().code());
        if (next.departure().isBefore(last.arrival().plus(MIN_CONNECTION)))
            throw new IllegalArgumentException("connection too short: " + next.key() + " leaves before " + last.key() + " lands + 45 min");
    }

    synchronized void addLeg(FlightReservation r) { legs.add(r); }

    synchronized BigDecimal total() {
        return legs.stream().filter(l -> l.status() == ReservationStatus.CONFIRMED)
                .map(FlightReservation::amount).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    /** Idempotent: a second pay() (double click, retry) does not charge again. */
    synchronized void pay(String cardToken) {
        if (paid) return;
        System.out.println("    charged " + total() + " to " + cardToken + " for itinerary " + id);
        paid = true;
    }
    synchronized boolean isPaid() { return paid; }
}
