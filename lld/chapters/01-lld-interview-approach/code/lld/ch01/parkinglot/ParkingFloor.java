package lld.ch01.parkinglot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Holds the spots on one floor and finds a free one. Not thread-safe on its own: ParkingLot calls it under its lock. */
public final class ParkingFloor {
    private final int number;
    private final List<ParkingSpot> spots = new ArrayList<>();

    public ParkingFloor(int number) { this.number = number; }

    public void addSpot(SpotType type, int count) {
        for (int i = 1; i <= count; i++) {
            spots.add(new ParkingSpot("F" + number + "-" + type.name().charAt(0) + i, type));
        }
    }

    /** First free spot, trying the vehicle's preferred (smallest) spot type first. */
    public Optional<ParkingSpot> findFreeSpot(VehicleType vehicleType) {
        for (SpotType wanted : vehicleType.fittingSpots()) {
            for (ParkingSpot s : spots) {
                if (s.type() == wanted && s.isFree()) return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public long freeCount(SpotType type) {
        return spots.stream().filter(s -> s.type() == type && s.isFree()).count();
    }
}
