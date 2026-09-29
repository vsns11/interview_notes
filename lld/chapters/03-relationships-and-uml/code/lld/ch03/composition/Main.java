package lld.ch03.composition;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot("City Centre");
        Map<SpotType, Integer> layout = new LinkedHashMap<>();
        layout.put(SpotType.COMPACT, 3);
        layout.put(SpotType.LARGE, 1);

        ParkingFloor ground = lot.addFloor(0, layout);
        lot.addFloor(1, Map.of(SpotType.MOTORBIKE, 4));

        System.out.println("Floor 0 spots: " + ground.spots());
        System.out.println("Total spots in lot: " + lot.totalSpots());
        System.out.println("Parked compact car at " + ground.park(SpotType.COMPACT));
        System.out.println("Free spots on floor 0: " + ground.freeSpots());

        try {
            ground.spots().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("Outsiders cannot change the parts: spots() is read-only");
        }

        lot.removeFloor(1);
        System.out.println("After removing floor 1, total spots: " + lot.totalSpots()
                + " (its 4 spots are gone with it)");
        System.out.println("Floors left: " + lot.floors().keySet());
    }
}
