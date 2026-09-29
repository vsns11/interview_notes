package lld.ch16.airline;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        var airline = new Airline("Global Air", new LastMinuteFare(new ClassMultiplierFare()), new EmailNotifier());
        var lhr = new Airport("LHR", "London");
        var nrt = new Airport("NRT", "Tokyo");
        var jfk = new Airport("JFK", "New York");

        var seats = List.of(
                new Seat("1A", SeatType.REGULAR, SeatClass.BUSINESS), new Seat("1B", SeatType.REGULAR, SeatClass.BUSINESS),
                new Seat("10A", SeatType.REGULAR, SeatClass.ECONOMY), new Seat("10B", SeatType.REGULAR, SeatClass.ECONOMY),
                new Seat("10C", SeatType.ACCESSIBLE, SeatClass.ECONOMY), new Seat("12A", SeatType.EXTRA_LEG_ROOM, SeatClass.ECONOMY_PLUS));
        var a1 = new Aircraft("G-GAAA", "A350-900", seats);
        var a2 = new Aircraft("G-GAAB", "B787-9", seats);
        var admin = new Admin("AD1", "Olga");
        airline.addAircraft(admin, a1);
        airline.addAircraft(admin, a2);

        System.out.println("1) Admin adds flights (routes) and weekly schedules, then publishes instances");
        var ga212 = new Flight("GA212", lhr, nrt, Duration.ofHours(14), a1, new BigDecimal("500.00"));
        ga212.addSchedule(new WeeklySchedule(DayOfWeek.TUESDAY, LocalTime.of(10, 0)));
        ga212.addSchedule(new WeeklySchedule(DayOfWeek.THURSDAY, LocalTime.of(10, 0)));
        var ga100 = new Flight("GA100", jfk, lhr, Duration.ofHours(7), a2, new BigDecimal("400.00"));
        ga100.addSchedule(new WeeklySchedule(DayOfWeek.WEDNESDAY, LocalTime.of(20, 0)));
        var ga300 = new Flight("GA300", lhr, jfk, Duration.ofHours(8), a2, new BigDecimal("450.00"));
        ga300.addSchedule(new WeeklySchedule(DayOfWeek.THURSDAY, LocalTime.of(12, 0)));
        var from = LocalDate.of(2026, 10, 5);
        var to = LocalDate.of(2026, 10, 19);
        var ga212Instances = airline.publish(admin, ga212, from, to);
        airline.publish(admin, ga100, from, to);
        airline.publish(admin, ga300, from, to);
        System.out.println("   GA212 instances: " + ga212Instances.stream().map(FlightInstance::key).toList());

        System.out.println("2) Search JFK->LHR on 7 Oct and LHR->NRT on 8 Oct");
        var leg1 = airline.search(jfk, lhr, LocalDate.of(2026, 10, 7)).getFirst();
        var leg2 = airline.search(lhr, nrt, LocalDate.of(2026, 10, 8)).getFirst();
        for (var fi : List.of(leg1, leg2))
            System.out.println("   " + fi.key() + " dep " + fi.departure() + " arr " + fi.arrival() + ", free seats " + fi.freeSeatCount());

        System.out.println("3) Priya books a two-leg itinerary for herself and her son");
        var priya = new Customer("C1", "Priya", "priya@example.com");
        var me = new Passenger("Priya Shah", "P-001");
        var son = new Passenger("Arjun Shah", "P-002");
        var bookedOn = LocalDate.of(2026, 10, 1);
        var it = airline.newItinerary(priya);
        airline.book(it, leg1, ordered(me, "10A", son, "10B"), bookedOn);
        airline.book(it, leg2, ordered(me, "1A", son, "1B"), bookedOn);
        System.out.println("   itinerary total " + it.total() + " (2 x 400 economy + 2 x 1250 business)");
        it.pay("tok_priya_card");
        it.pay("tok_priya_card");   // double click: no second charge

        System.out.println("4) Bad connection is rejected");
        var it2 = airline.newItinerary(priya);
        airline.book(it2, leg1, Map.of(me, "12A"), bookedOn);
        try { airline.book(it2, ga212Instances.getFirst(), Map.of(me, "10A"), bookedOn); }
        catch (IllegalArgumentException e) { System.out.println("   rejected: " + e.getMessage()); }
        airline.cancelItinerary(it2);

        System.out.println("5) 20 customers race for seat 10C on " + leg2.key());
        var winners = new AtomicInteger();
        var go = new CountDownLatch(1);
        var raceAirline = airline;   // effectively-final alias for the lambdas
        try (var pool = Executors.newFixedThreadPool(20)) {
            for (int i = 0; i < 20; i++) {
                int n = i;
                pool.submit(() -> {
                    go.await();
                    var c = new Customer("R" + n, "Racer" + n, "racer" + n + "@example.com");
                    var r = raceAirline.book(raceAirline.newItinerary(c), leg2, Map.of(new Passenger("Racer" + n, "X" + n), "10C"), bookedOn);
                    if (r != null) winners.incrementAndGet();
                    return null;
                });
            }
            go.countDown();
        }
        System.out.println("   successful bookings of 10C: " + winners.get() + ", free seats left: " + leg2.freeSeatCount());

        System.out.println("6) Walk-in at the front desk, last-minute (booked 7 Oct for 8 Oct): pays +25%");
        var desk = new FrontDeskOfficer("FD1", "Tom");
        var late = airline.newItinerary(new Customer("C2", "Leo", "leo@example.com"));
        var r = airline.bookFor(desk, late, leg2, Map.of(new Passenger("Leo Park", "P-003"), "12A"), LocalDate.of(2026, 10, 7));
        System.out.println("   economy-plus fare 500 x 1.3 = 650.00, charged " + r.amount());

        System.out.println("7) Admin assigns crew");
        var kim = new CrewMember("K1", "Capt. Kim", CrewRole.PILOT);
        var lee = new CrewMember("L1", "F/O Lee", CrewRole.CO_PILOT);
        var ana = new CrewMember("A1", "Ana", CrewRole.CABIN_CREW);
        for (var m : List.of(kim, lee, ana)) airline.assignCrew(admin, leg2, m);
        var ga300Oct8 = airline.search(lhr, jfk, LocalDate.of(2026, 10, 8)).getFirst();
        try { airline.assignCrew(admin, ga300Oct8, kim); }
        catch (IllegalStateException e) { System.out.println("   rejected: " + e.getMessage()); }
        System.out.println("   Kim's schedule: " + airline.scheduleFor(kim).stream().map(FlightInstance::key).toList());

        System.out.println("8) Admin cancels " + leg2.key() + ": every passenger and crew member is told");
        airline.cancelFlight(admin, leg2);
        System.out.println("   search LHR->NRT on 8 Oct now returns " + airline.search(lhr, nrt, LocalDate.of(2026, 10, 8)).size() + " flights");

        System.out.println("9) Priya cancels the rest of her trip; seats on " + leg1.key() + " come back");
        airline.cancelItinerary(it);
        System.out.println("   free economy seats on " + leg1.key() + ": " + leg1.availableSeats(SeatClass.ECONOMY).stream().map(Seat::number).toList());
    }

    static Map<Passenger, String> ordered(Passenger a, String sa, Passenger b, String sb) {
        var m = new LinkedHashMap<Passenger, String>();
        m.put(a, sa);
        m.put(b, sb);
        return m;
    }
}
