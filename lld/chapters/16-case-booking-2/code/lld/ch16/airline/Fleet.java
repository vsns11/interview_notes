package lld.ch16.airline;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

record Aircraft(String registration, String model, List<Seat> seats) {
    Aircraft { seats = List.copyOf(seats); }
}

/** A recurring slot: "every Tuesday at 10:00". */
record WeeklySchedule(DayOfWeek day, LocalTime departure) {}

/**
 * A Flight is the ROUTE (GA212 LHR->NRT). It creates one FlightInstance per scheduled day.
 * All times are in one reference zone (UTC) to keep the example small.
 */
final class Flight {
    private final String number;
    private final Airport from;
    private final Airport to;
    private final Duration duration;
    private final Aircraft aircraft;
    private final BigDecimal baseFare;
    private final List<WeeklySchedule> schedules = new ArrayList<>();

    Flight(String number, Airport from, Airport to, Duration duration, Aircraft aircraft, BigDecimal baseFare) {
        this.number = number; this.from = from; this.to = to;
        this.duration = duration; this.aircraft = aircraft; this.baseFare = baseFare;
    }
    String number() { return number; }
    Airport from() { return from; }
    Airport to() { return to; }
    Duration duration() { return duration; }
    Aircraft aircraft() { return aircraft; }
    BigDecimal baseFare() { return baseFare; }

    void addSchedule(WeeklySchedule s) { schedules.add(s); }

    List<FlightInstance> generateInstances(LocalDate fromDate, LocalDate toDateExclusive) {
        List<FlightInstance> out = new ArrayList<>();
        fromDate.datesUntil(toDateExclusive).forEach(d -> schedules.stream()
                .filter(s -> s.day() == d.getDayOfWeek())
                .forEach(s -> out.add(new FlightInstance(this, LocalDateTime.of(d, s.departure())))));
        return out;
    }
}
