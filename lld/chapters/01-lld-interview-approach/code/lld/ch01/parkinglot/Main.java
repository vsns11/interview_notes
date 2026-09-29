package lld.ch01.parkinglot;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ParkingFloor ground = new ParkingFloor(0);
        ground.addSpot(SpotType.MOTORBIKE, 1);
        ground.addSpot(SpotType.COMPACT, 1);
        ground.addSpot(SpotType.LARGE, 1);
        ParkingLot lot = new ParkingLot(List.of(ground), new HourlyRate());

        Vehicle car   = new Vehicle("KA-01-1234", VehicleType.CAR);
        Vehicle truck = new Vehicle("KA-02-9999", VehicleType.TRUCK);
        Vehicle car2  = new Vehicle("KA-03-5555", VehicleType.CAR);

        Ticket t1 = lot.park(car, 0).orElseThrow();
        System.out.println(t1.id() + ": " + car.plate() + " -> spot " + t1.spot().id());
        Ticket t2 = lot.park(truck, 10).orElseThrow();
        System.out.println(t2.id() + ": " + truck.plate() + " -> spot " + t2.spot().id());
        System.out.println(car2.plate() + " -> " + lot.park(car2, 20).map(Ticket::id).orElse("refused, no suitable spot"));

        System.out.println("free compact spots: " + lot.freeSpots(SpotType.COMPACT));
        System.out.println(t1.id() + " exits after 150 min, pays " + money(lot.exit(t1.id(), 150)));
        System.out.println(t2.id() + " exits after 50 min, pays " + money(lot.exit(t2.id(), 60)));

        lot.setPricing(new FlatWeekendRate());          // swap the strategy at run time
        Ticket t3 = lot.park(car2, 200).orElseThrow();
        System.out.println(t3.id() + ": " + car2.plate() + " -> spot " + t3.spot().id()
                + ", weekend exit pays " + money(lot.exit(t3.id(), 500)));

        try {
            lot.exit(t1.id(), 600);
        } catch (IllegalArgumentException e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    private static String money(BigDecimal amount) { return "$" + amount.toPlainString(); }
}
