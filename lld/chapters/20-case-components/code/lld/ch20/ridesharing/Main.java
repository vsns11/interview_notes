package lld.ch20.ridesharing;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        StandardPricing standard = new StandardPricing(Map.of(
                VehicleType.ECONOMY, new RateCard(bd("2.50"), bd("1.20"), bd("0.25"), bd("5.00")),
                VehicleType.PREMIUM, new RateCard(bd("5.00"), bd("2.00"), bd("0.40"), bd("10.00")),
                VehicleType.VAN, new RateCard(bd("4.00"), bd("1.80"), bd("0.30"), bd("8.00"))));
        RideSharingService svc = new RideSharingService(standard, new NearestDriverMatcher(10));
        svc.addListener((trip, event) -> System.out.println("  [" + trip.id() + "] " + event));

        svc.addRider(new Rider("R1", "Riya", "+91-1", new WalletPayment(bd("50.00"))));
        svc.addRider(new Rider("R2", "Sam", "+91-2", new CashPayment()));
        Driver d1 = new Driver("D1", "Dev", "+91-3", new Vehicle("KA-01", VehicleType.ECONOMY, 4), new Location(0, 0));
        Driver d2 = new Driver("D2", "Ira", "+91-4", new Vehicle("KA-02", VehicleType.ECONOMY, 4), new Location(2, 1));
        Driver d3 = new Driver("D3", "Om", "+91-5", new Vehicle("KA-03", VehicleType.PREMIUM, 4), new Location(1, 1));
        Driver d4 = new Driver("D4", "Zoe", "+91-6", new Vehicle("KA-04", VehicleType.ECONOMY, 4), new Location(1, 0));
        for (Driver d : new Driver[]{d1, d2, d3, d4}) svc.addDriver(d);
        d1.setOnline(true);
        d2.setOnline(true);
        d3.setOnline(true); // d4 stays offline, so it is never offered anything

        System.out.println("== 1. Request, first driver declines, second accepts, full ride");
        Trip t1 = svc.requestRide("R1", new Location(1, 0), new Location(5, 3), VehicleType.ECONOMY);
        svc.respond(t1.id(), "D1", false);
        svc.respond(t1.id(), "D2", true);
        System.out.println("  D2 available while on trip? " + d2.isAvailable());
        svc.startTrip(t1.id());
        svc.startTrip(t1.id());                 // illegal: already started
        svc.endTrip(t1.id(), 12);               // 2.50 + 7*1.20 + 12*0.25 = 13.90
        System.out.println("  D2 now at " + d2.location() + ", available? " + d2.isAvailable());
        svc.rateDriver(t1.id(), 5);
        svc.rateRider(t1.id(), 4);
        System.out.println("  rate driver twice allowed? " + svc.rateDriver(t1.id(), 1)
                + "; D2 rating " + d2.rating() + ", R1 rating " + svc.rider("R1").rating());

        System.out.println("== 2. Surge: wrap the same rule in a decorator (x1.5)");
        svc.setPricing(new SurgePricing(standard, bd("1.5")));
        Trip t2 = svc.requestRide("R2", new Location(0, 1), new Location(0, 2), VehicleType.ECONOMY);
        svc.respond(t2.id(), "D1", true);
        svc.startTrip(t2.id());
        svc.endTrip(t2.id(), 3);                // max(2.50+1.20+0.75, 5.00) = 5.00, x1.5 = 7.50
        svc.setPricing(standard);

        System.out.println("== 3. Rider cancels after accept; cancel after start is refused");
        Trip t3 = svc.requestRide("R1", new Location(1, 1), new Location(3, 3), VehicleType.PREMIUM);
        svc.respond(t3.id(), "D3", true);
        svc.cancelTrip(t3.id());
        System.out.println("  D3 released? " + d3.isAvailable());
        Trip t4 = svc.requestRide("R1", new Location(1, 1), new Location(3, 3), VehicleType.PREMIUM);
        svc.respond(t4.id(), "D3", true);
        svc.startTrip(t4.id());
        svc.cancelTrip(t4.id());
        svc.endTrip(t4.id(), 10);               // 5 + 4*2 + 10*0.4 = 17.00 -> wallet has 36.10

        System.out.println("== 4. Nobody available for a VAN");
        svc.requestRide("R2", new Location(0, 0), new Location(1, 1), VehicleType.VAN);

        System.out.println("== 5. Race: 40 riders, 10 drivers, all accept at once");
        RideSharingService race = new RideSharingService(standard, new NearestDriverMatcher(100));
        for (int i = 0; i < 10; i++) {
            Driver d = new Driver("X" + i, "x", "-", new Vehicle("P" + i, VehicleType.ECONOMY, 4), new Location(i, 0));
            d.setOnline(true);
            race.addDriver(d);
        }
        for (int i = 0; i < 40; i++) race.addRider(new Rider("Q" + i, "q", "-", new CashPayment()));
        AtomicInteger accepted = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(16);
        for (int i = 0; i < 40; i++) {
            String rider = "Q" + i;
            pool.submit(() -> {
                go.await();
                Trip t = race.requestRide(rider, new Location(0, 0), new Location(3, 3), VehicleType.ECONOMY);
                for (Driver c = t.currentCandidate(); c != null && t.status() == TripStatus.REQUESTED;
                     c = t.currentCandidate()) {
                    if (race.respond(t.id(), c.id(), true)) { accepted.incrementAndGet(); break; }
                }
                return null;
            });
        }
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("  trips accepted = " + accepted.get() + " (never more than 10 drivers)");
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }
}
