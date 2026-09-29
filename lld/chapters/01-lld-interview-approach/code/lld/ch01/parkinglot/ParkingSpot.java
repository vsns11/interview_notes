package lld.ch01.parkinglot;

/** Knows whether it is free and which vehicle is in it. */
public final class ParkingSpot {
    private final String id;
    private final SpotType type;
    private Vehicle vehicle;                 // null = free

    public ParkingSpot(String id, SpotType type) { this.id = id; this.type = type; }

    public boolean isFree()      { return vehicle == null; }
    public SpotType type()       { return type; }
    public String id()           { return id; }

    void occupy(Vehicle v) {                 // package-private: only the lot may change a spot
        if (!isFree()) throw new IllegalStateException(id + " is taken");
        vehicle = v;
    }
    void release() { vehicle = null; }
}
