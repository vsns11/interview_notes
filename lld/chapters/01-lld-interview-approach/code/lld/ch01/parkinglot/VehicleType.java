package lld.ch01.parkinglot;

import java.util.List;

/** Each vehicle type knows which spot types fit it, smallest first. */
public enum VehicleType {
    MOTORBIKE(List.of(SpotType.MOTORBIKE, SpotType.COMPACT)),
    CAR(List.of(SpotType.COMPACT, SpotType.LARGE)),
    TRUCK(List.of(SpotType.LARGE));

    private final List<SpotType> fits;

    VehicleType(List<SpotType> fits) { this.fits = fits; }

    public List<SpotType> fittingSpots() { return fits; }
}
