package lld.ch21.propertyhunt;

import java.util.Set;

public class Property {
    private final int id;
    private final String ownerId;
    private final String title;
    private final String location;          // stored lower-case for case-insensitive match
    private final Set<String> nearby;       // bonus: nearby locations, lower-case
    private final long price;
    private final ListingType type;
    private final double sizeSqft;
    private final int rooms;
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    Property(int id, String ownerId, String title, String location, Set<String> nearby, long price,
             ListingType type, double sizeSqft, int rooms) {
        this.id = id; this.ownerId = ownerId; this.title = title; this.location = location;
        this.nearby = Set.copyOf(nearby); this.price = price; this.type = type;
        this.sizeSqft = sizeSqft; this.rooms = rooms;
    }

    public int id() { return id; }
    public String ownerId() { return ownerId; }
    public String title() { return title; }
    public String location() { return location; }
    public Set<String> nearby() { return nearby; }
    public long price() { return price; }
    public ListingType type() { return type; }
    public double sizeSqft() { return sizeSqft; }
    public int rooms() { return rooms; }
    public synchronized PropertyStatus status() { return status; }

    synchronized void markSold() {
        if (status == PropertyStatus.SOLD) throw new HuntException("Property " + id + " is already sold");
        status = PropertyStatus.SOLD;
    }
}
