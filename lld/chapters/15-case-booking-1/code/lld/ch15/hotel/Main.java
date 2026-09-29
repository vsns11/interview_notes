package lld.ch15.hotel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        SimClock clock = new SimClock(Instant.parse("2026-07-01T09:00:00Z"));
        Notifier notifier = (g, m) -> System.out.println("  [notify " + g + "] " + m);
        Hotel hotel = new Hotel("Seaside Hotels");
        HotelLocation goa = new HotelLocation("Goa"), kochi = new HotelLocation("Kochi");
        hotel.addLocation(goa);
        hotel.addLocation(kochi);
        FrontDesk desk = new FrontDesk(goa, clock, notifier);
        Manager boss = new Manager("MG1", "Maya");
        desk.addRoom(boss, new Room("101", RoomStyle.DELUXE, usd("120.00"), false));
        desk.addRoom(boss, new Room("102", RoomStyle.DELUXE, usd("120.00"), false));
        desk.addRoom(boss, new Room("201", RoomStyle.FAMILY_SUITE, usd("250.00"), false));
        kochi.addRoom(new Room("K-1", RoomStyle.DELUXE, usd("90.00"), true));
        Room r101 = goa.room("101");
        Receptionist rita = new Receptionist("RC1", "Rita");
        desk.hire(boss, rita);
        desk.hire(boss, new Server("SV1", "Sam"));
        desk.hire(boss, new Housekeeper("HK1", "Hari"));
        System.out.println("== 0. Manager adds rooms and hires staff: " + desk.staff() + " ==\n");

        LocalDate jul10 = LocalDate.of(2026, 7, 10), jul13 = LocalDate.of(2026, 7, 13);
        System.out.println("== 1. Search DELUXE for Jul 10-13 across locations ==");
        hotel.search(RoomStyle.DELUXE, jul10, jul13).forEach((loc, rooms) ->
                System.out.println("  " + loc + " -> " + rooms.stream().map(Room::number).toList()));

        System.out.println("\n== 2. 6 guests try to book room 101 for overlapping dates at once ==");
        ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(6)) {
            for (int i = 1; i <= 6; i++) {
                Guest g = new Guest("G" + i, "Guest " + i, "g" + i + "@mail.com");
                LocalDate out = jul10.plusDays(2 + i % 2);     // Jul 10-12 or Jul 10-13: all overlap
                pool.submit(() -> {
                    go.await();
                    try { desk.book(g, r101, jul10, out, new CreditCardTransaction(usd("120.00"), g.name())); results.add("OK"); }
                    catch (IllegalStateException e) { results.add("NO"); }
                    return null;
                });
            }
            go.countDown();
        }
        System.out.println("  successful bookings = " + results.stream().filter("OK"::equals).count() + " of 6");
        System.out.println("  who booked 101? " + desk.whoBooked("101"));

        System.out.println("\n== 3. Back-to-back stays share the changeover day ==");
        Guest asha = new Guest("ASHA", "Asha", "asha@mail.com");
        RoomBooking stay = desk.bookFor(rita, asha, goa.room("102"), jul10, jul13, new CashTransaction(usd("120.00"), usd("200.00")));
        RoomBooking next = desk.book(new Guest("BEN", "Ben", "ben@mail.com"), goa.room("102"), jul13,
                LocalDate.of(2026, 7, 15), new CheckTransaction(usd("120.00"), "HDFC", "000123"));
        System.out.println("  rooms booked by ASHA: " + desk.roomsBookedBy("ASHA"));

        System.out.println("\n== 4. Cancellation: >24h before check-in = full refund, later = none ==");
        RoomBooking suite = desk.book(asha, goa.room("201"), jul10, jul13, new CreditCardTransaction(usd("250.00"), "Asha"));
        Guest carl = new Guest("CARL", "Carl", "carl@mail.com");
        RoomBooking lastMinute = desk.book(carl, goa.room("201"), LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 3),
                new CreditCardTransaction(usd("250.00"), "Carl"));
        clock.advance(Duration.ofHours(10));               // Jul 1 19:00: Carl's check-in (Jul 2 14:00) is 19h away
        desk.cancel(carl, lastMinute.reservationNumber());
        desk.cancel(asha, suite.reservationNumber());      // Asha's is 8+ days away

        System.out.println("\n== 5. Reminders, check-in (key), room service, check-out (invoice) ==");
        clock.advance(Duration.ofDays(8).plusHours(13));   // Jul 10 08:00
        desk.sendReminders();
        clock.advance(Duration.ofHours(6));                // Jul 10 14:00
        RoomKey key = desk.checkIn(rita, stay.reservationNumber());
        System.out.println("  issued key " + key.keyId() + " active=" + key.isActive());
        Server sam = new Server("SV1", "Sam");
        desk.addCharge(sam, stay.reservationNumber(), new RoomService("Extra towels", usd("5.00")));
        desk.addCharge(sam, stay.reservationNumber(), new KitchenService("Fish curry dinner", usd("24.00")));
        desk.addCharge(sam, stay.reservationNumber(), new Amenity("Spa access", usd("30.00")));
        clock.advance(Duration.ofDays(2).plusHours(19));   // Jul 13 09:00: checkout at 11:00 today
        desk.sendReminders();
        try { desk.checkOut(rita, stay.reservationNumber(), total -> new CashTransaction(total, usd("100.00"))); }
        catch (IllegalStateException e) { System.out.println("  cash short: " + e.getMessage()); }
        Invoice inv = desk.checkOut(rita, stay.reservationNumber(), total -> new CreditCardTransaction(total, "Asha"));
        inv.items().forEach(i -> System.out.printf("    %-28s %10s%n", i.description(), Money.fmt(i.amount())));
        System.out.printf("    %-28s %10s%n", "TOTAL", Money.fmt(inv.total()));
        System.out.println("  key active after checkout? " + key.isActive() + ", room 102 is " + goa.room("102").status());

        System.out.println("\n== 6. Housekeeping log ==");
        desk.logHousekeeping(new Housekeeper("HK1", "Hari"), goa.room("102"), "Full clean, linen change", 40);
        goa.room("102").housekeepingLog().forEach(h -> System.out.println("  " + h));
        System.out.println("  room 102 is " + goa.room("102").status());
    }

    private static BigDecimal usd(String amount) { return new BigDecimal(amount); }
}
