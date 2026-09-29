package lld.ch15.moviebooking;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        SimClock clock = new SimClock(Instant.parse("2026-05-01T12:00:00Z"));
        FakeGateway bank = new FakeGateway(Set.of("0002"));
        SeatLockProvider locks = new InMemorySeatLockProvider(Duration.ofMinutes(5), clock);
        Notifier notifier = new ConsoleNotifier();
        BookingService svc = new BookingService(locks, bank, new CutoffRefundPolicy(Duration.ofHours(2)),
                notifier, clock);
        Catalog catalog = new Catalog(notifier);

        // ---- venue: 2 rows x 5 seats; row A regular, row B premium
        City blr = new City("Bengaluru", "KA");
        Cinema pvr = new Cinema("PVR Forum", blr);
        List<HallSeat> seats = new ArrayList<>();
        for (char row : new char[]{'A', 'B'})
            for (int n = 1; n <= 5; n++) seats.add(new HallSeat("" + row + n, row == 'A' ? SeatType.REGULAR : SeatType.PREMIUM));
        CinemaHall audi1 = new CinemaHall("Audi 1", seats);
        pvr.addHall(audi1);

        Movie inception = new Movie("MV1", "Inception", "English", "Sci-Fi", LocalDate.of(2010, 7, 16), 148);
        Movie rrr = new Movie("MV2", "RRR", "Telugu", "Action", LocalDate.of(2022, 3, 25), 187);
        Map<SeatType, BigDecimal> prices = Map.of(SeatType.REGULAR, new BigDecimal("200.00"),
                SeatType.PREMIUM, new BigDecimal("350.00"));
        Show evening = new Show("SH1", inception, pvr, audi1, LocalDateTime.of(2026, 5, 1, 18, 0), prices);
        Show clash = new Show("SH2", rrr, pvr, audi1, LocalDateTime.of(2026, 5, 1, 19, 0), prices);

        System.out.println("== 1. Admin adds movies (subscribers told) and schedules shows; the hall refuses an overlap ==");
        catalog.addMovie(inception);
        catalog.addMovie(rrr);
        System.out.println("  add SH1 Inception 18:00 -> " + catalog.addShow(evening));
        System.out.println("  add SH2 RRR 19:00 same hall -> " + catalog.addShow(clash));

        System.out.println("\n== 2. Search ==");
        System.out.println("  city Bengaluru -> " + titles(catalog.searchByCity("bengaluru")));
        System.out.println("  genre Action   -> " + titles(catalog.searchByGenre("action")) + " (no show yet, but indexed)");
        System.out.println("  Inception shows in Bengaluru -> "
                + catalog.showsOf(inception, "Bengaluru").stream().map(s -> s.id() + "@" + s.start()).toList());

        System.out.println("\n== 3. 10 customers click B3+B4 at the same instant ==");
        List<Customer> crowd = new ArrayList<>();
        for (int i = 1; i <= 10; i++) crowd.add(new Customer("U" + i, "User " + i));
        ConcurrentLinkedQueue<Booking> won = new ConcurrentLinkedQueue<>();
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(10)) {
            for (Customer c : crowd)
                pool.submit(() -> {
                    go.await();
                    try { won.add(svc.hold(c, evening, List.of("B3", "B4"))); }
                    catch (SeatUnavailableException ignored) { }
                    return null;
                });
            go.countDown();
        }
        Booking first = won.peek();
        System.out.println("  holds granted = " + won.size() + " (the other 9 saw 'not available')");
        System.out.println(svc.seatMap(evening));

        System.out.println("\n== 4. The winner pays by card with a 10% coupon ==");
        svc.pay(first.id(), new CardPayment("4242"), new Coupon("FIRST10", 10));
        System.out.println("  paying twice is harmless -> " + svc.pay(first.id(), new CardPayment("4242"), null).status());
        System.out.println(svc.seatMap(evening));

        System.out.println("\n== 5. Hold expires (TTL 5 min) before paying ==");
        Customer asha = new Customer("ASHA", "Asha"), ravi = new Customer("RAVI", "Ravi");
        Booking slow = svc.hold(asha, evening, List.of("A1", "A2"));
        clock.advance(Duration.ofMinutes(6));
        Booking fast = svc.hold(ravi, evening, List.of("A1", "A2"));
        System.out.println("  Ravi re-held A1,A2 after Asha's hold expired -> " + fast.id());
        svc.pay(slow.id(), new CardPayment("4242"), null);
        svc.pay(fast.id(), new CashPayment(), null);

        System.out.println("\n== 6. Hold expires WHILE the bank is charging -> refund (compensation) ==");
        Customer meena = new Customer("MEENA", "Meena"), kiran = new Customer("KIRAN", "Kiran");
        Booking victim = svc.hold(meena, evening, List.of("A5"));
        bank.setDuringCharge(() -> {                       // bank is slow: 6 minutes pass, Kiran grabs A5
            clock.advance(Duration.ofMinutes(6));
            svc.hold(kiran, evening, List.of("A5"));
        });
        svc.pay(victim.id(), new CardPayment("4242"), null);
        bank.setDuringCharge(() -> {});
        System.out.println("  " + victim);

        System.out.println("\n== 7. Declined card releases the seats immediately ==");
        Booking declined = svc.hold(new Customer("NEHA", "Neha"), evening, List.of("B1"));
        svc.pay(declined.id(), new CardPayment("0002"), null);
        System.out.println("  B1 held by anyone now? " + locks.lockedSeats(evening).contains("B1"));

        System.out.println("\n== 8. Blocked customer, then cancellation with refund ==");
        new Admin("AD1", "Admin").block(crowd.get(9));
        try { svc.hold(crowd.get(9), evening, List.of("B5")); }
        catch (IllegalStateException e) { System.out.println("  REFUSED: " + e.getMessage()); }
        svc.cancel(first.id());                            // 12:06+ vs 18:00 show: more than 2h before
        System.out.println("  " + first);
        System.out.println(svc.seatMap(evening));
    }

    private static List<String> titles(List<Movie> ms) { return ms.stream().map(Movie::title).toList(); }
}
