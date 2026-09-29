package lld.ch03.composition;

/**
 * A part of a composition. The constructor is package-private, so only ParkingFloor
 * (same package) can create a spot. Outside code can read a spot but never make one.
 */
public final class ParkingSpot {
    private final String id;
    private final SpotType type;
    private boolean free = true;

    ParkingSpot(String id, SpotType type) {
        this.id = id;
        this.type = type;
    }

    public String id() { return id; }
    public SpotType type() { return type; }
    public boolean isFree() { return free; }
    void occupy() { free = false; }

    @Override public String toString() { return id + ":" + type; }
}
