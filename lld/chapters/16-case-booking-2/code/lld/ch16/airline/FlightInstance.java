package lld.ch16.airline;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One seat on one flight instance: 12A can be free on Tuesday and taken on Thursday. */
final class FlightSeat {
    private final Seat seat;
    private String reservationNumber;   // null = free
    FlightSeat(Seat seat) { this.seat = seat; }
    Seat seat() { return seat; }
    String reservationNumber() { return reservationNumber; }
    boolean isFree() { return reservationNumber == null; }
    void assign(String resNo) { reservationNumber = resNo; }
    void free() { reservationNumber = null; }
}

/** The route flown on one date: owns its seats, its crew list and its status. */
final class FlightInstance {
    private final Flight flight;
    private final LocalDateTime departure;
    private final Map<String, FlightSeat> seats = new LinkedHashMap<>();
    private final List<CrewMember> crew = new ArrayList<>();
    private FlightStatus status = FlightStatus.SCHEDULED;

    FlightInstance(Flight flight, LocalDateTime departure) {
        this.flight = flight;
        this.departure = departure;
        flight.aircraft().seats().forEach(s -> seats.put(s.number(), new FlightSeat(s)));
    }

    Flight flight() { return flight; }
    LocalDateTime departure() { return departure; }
    LocalDateTime arrival() { return departure.plus(flight.duration()); }
    synchronized FlightStatus status() { return status; }
    synchronized List<CrewMember> crew() { return List.copyOf(crew); }
    String key() { return flight.number() + "@" + departure.toLocalDate(); }

    synchronized List<Seat> availableSeats(SeatClass c) {
        return seats.values().stream().filter(FlightSeat::isFree).map(FlightSeat::seat)
                .filter(s -> s.seatClass() == c).toList();
    }

    synchronized int freeSeatCount() { return (int) seats.values().stream().filter(FlightSeat::isFree).count(); }

    /** All-or-nothing: a family of three either gets all three seats or none. */
    synchronized Map<Passenger, Seat> holdSeats(Map<Passenger, String> request, String reservationNumber) {
        if (status != FlightStatus.SCHEDULED && status != FlightStatus.DELAYED)
            throw new IllegalStateException(key() + " is " + status);
        for (var seatNo : request.values()) {
            var fs = seats.get(seatNo);
            if (fs == null) throw new IllegalArgumentException("no seat " + seatNo + " on " + key());
            if (!fs.isFree()) return Map.of();
        }
        if (request.values().stream().distinct().count() != request.size())
            throw new IllegalArgumentException("two passengers asked for the same seat");
        Map<Passenger, Seat> out = new LinkedHashMap<>();
        request.forEach((p, no) -> { var fs = seats.get(no); fs.assign(reservationNumber); out.put(p, fs.seat()); });
        return out;
    }

    synchronized void releaseSeats(String reservationNumber) {
        seats.values().stream().filter(fs -> reservationNumber.equals(fs.reservationNumber())).forEach(FlightSeat::free);
    }

    synchronized void addCrew(CrewMember m) { crew.add(m); }
    synchronized void setStatus(FlightStatus s) { status = s; }

    boolean overlaps(FlightInstance other) {
        return departure.isBefore(other.arrival()) && other.departure().isBefore(arrival());
    }
}
