package lld.ch16.carrental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static BigDecimal usd(String s) { return new BigDecimal(s); }

    public static void main(String[] args) throws Exception {
        var pricing = new WeeklyDiscountPricing(new DailyRatePricing(Map.of(
                VehicleType.CAR, usd("50.00"), VehicleType.SUV, usd("80.00"), VehicleType.TRUCK, usd("100.00"),
                VehicleType.VAN, usd("90.00"), VehicleType.MOTORCYCLE, usd("35.00"))));
        var service = new ReservationService(pricing, new FlatLateFee(usd("40.00")), new ConsoleNotifier());
        var system = new CarRentalSystem(service);

        var sfo = new Location("SFO", "SFO Airport");
        var dtn = new Location("DTN", "Downtown");
        var sfoInv = system.addLocation(sfo).inventory();
        sfoInv.add(new Vehicle("VH-001", "7ABC123", "Toyota", "RAV4", VehicleType.SUV, 5, "A-12", 20_100));
        sfoInv.add(new Vehicle("VH-002", "7ABC124", "Honda", "CR-V", VehicleType.SUV, 5, "A-13", 15_400));
        sfoInv.add(new Vehicle("VH-003", "8XYZ001", "Honda", "Civic", VehicleType.CAR, 5, "B-02", 9_800));
        system.addLocation(dtn).inventory().add(new Vehicle("VH-010", "9VAN555", "Ford", "Transit", VehicleType.VAN, 12, "C-01", 30_000));

        var alice = new Member("M1", "Alice", "alice@example.com");
        var today = LocalDate.of(2026, 10, 1);
        var trip = new DateRange(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 13));

        System.out.println("1) Search SUVs at SFO for " + trip);
        sfoInv.search(VehicleType.SUV, trip).forEach(v -> System.out.println("   " + v.barcode() + " " + v.describe() + " stall " + v.parkingStall()));

        System.out.println("2) Alice reserves VH-001 (pick up SFO, return Downtown) and adds extras");
        var rav4 = sfoInv.scan("VH-001").orElseThrow();
        var r1 = service.reserve(alice, rav4, trip, sfo, dtn, today).orElseThrow();
        r1.addAddOn(new RentalInsurance("Collision damage waiver", usd("15.00")));
        r1.addAddOn(new Equipment("Child seat", usd("8.00")));
        r1.addAddOn(new Service("Roadside assistance", usd("25.00")));
        r1.addDriver(new AdditionalDriver("Sam", "D1234567"));

        System.out.println("3) 8 members race for VH-002 on the same dates");
        var crv = sfoInv.scan("VH-002").orElseThrow();
        var wins = new AtomicInteger();
        var winnerId = new String[1];
        var start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 8; i++) {
                var m = new Member("M" + (10 + i), "Racer" + i, "racer" + i + "@example.com");
                pool.submit(() -> {
                    start.await();
                    service.reserve(m, crv, new DateRange(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 14)), sfo, sfo, today)
                           .ifPresent(r -> { wins.incrementAndGet(); synchronized (winnerId) { winnerId[0] = r.id(); } });
                    return null;
                });
            }
            start.countDown();
        }
        System.out.println("   successful reservations for VH-002: " + wins.get());
        System.out.println("   SUVs free for " + trip + " now: " + sfoInv.search(VehicleType.SUV, trip).size());

        System.out.println("4) Daily reminder job on 2026-10-09");
        service.sendReminders(LocalDate.of(2026, 10, 9));

        System.out.println("5) Pick up on 10 Oct, return at Downtown on 15 Oct (due 13 Oct)");
        service.pickUp(r1.id(), LocalDate.of(2026, 10, 10));
        System.out.println("   VH-001 status while out: " + rav4.status());
        service.sendReminders(LocalDate.of(2026, 10, 14));
        service.returnVehicle(r1.id(), LocalDate.of(2026, 10, 15), 20_640);
        rav4.addLog(LocalDate.of(2026, 10, 15), VehicleLogType.CLEANING, "interior clean by worker W7");

        System.out.println("6) Alice's bill");
        r1.bill().items().forEach(i -> System.out.printf("   %-40s %-10s %8s%n", i.description(), i.type(), i.amount()));
        System.out.println("   TOTAL " + r1.bill().total());
        r1.bill().settle(new CardPayment("tok_alice_visa"));
        System.out.println("   paid: " + r1.bill().isPaid() + ", settle again (double click) -> "
                + r1.bill().settle(new CardPayment("tok_alice_visa")) + ", no second charge");

        System.out.println("7) Who rented VH-001? What has Alice rented?");
        service.byVehicle("VH-001").forEach(r -> System.out.println("   " + r.id() + " -> " + r.member().name() + " " + r.period() + " " + r.status()));
        service.byMember("M1").forEach(r -> System.out.println("   Alice: " + r.id() + " " + r.vehicle().model()));

        System.out.println("8) Vehicle log for VH-001");
        rav4.history().forEach(e -> System.out.println("   " + e.date() + " " + e.type() + " " + e.note()));

        System.out.println("9) The VH-002 winner cancels; the days come back");
        service.cancel(winnerId[0], LocalDate.of(2026, 10, 2));
        System.out.println("   SUVs free for " + trip + ": " + sfoInv.search(VehicleType.SUV, trip).size());

        System.out.println("10) A 7-day Civic rental gets the weekly discount");
        var week = new DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 8));
        var r3 = service.reserve(alice, sfoInv.scan("VH-003").orElseThrow(), week, sfo, sfo, today).orElseThrow();
        System.out.println("   base charge " + r3.bill().total() + " (7 x 50 = 350, minus 10%)");

        System.out.println("11) Rules are enforced");
        try { r1.addAddOn(new Equipment("Ski rack", usd("10.00"))); }
        catch (IllegalStateException e) { System.out.println("   rejected: " + e.getMessage()); }
        try { new DateRange(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5)); }
        catch (IllegalArgumentException e) { System.out.println("   rejected: " + e.getMessage()); }
    }
}
