package lld.ch20.ridesharing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** A point on a city grid. Distance is measured along streets (Manhattan distance), in km. */
record Location(int x, int y) {
    int distanceTo(Location o) { return Math.abs(x - o.x) + Math.abs(y - o.y); }
    @Override public String toString() { return "(" + x + "," + y + ")"; }
}

enum VehicleType { ECONOMY, PREMIUM, VAN }

record Vehicle(String plate, VehicleType type, int seats) {}

/** What riders and drivers share. Rating = running total + count (average is all anyone reads). */
abstract class Account {
    private final String id;
    private final String name;
    private final String phone;
    private int ratingTotal;
    private int ratingCount;
    private final List<String> finishedTrips = new ArrayList<>();

    Account(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    String id() { return id; }
    String name() { return name; }
    String phone() { return phone; }

    synchronized void addRating(int stars) {
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("stars must be 1..5");
        ratingTotal += stars;
        ratingCount++;
    }

    synchronized BigDecimal rating() {
        return ratingCount == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(ratingTotal).divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP);
    }

    synchronized void recordTrip(String tripId) { finishedTrips.add(tripId); }
    synchronized List<String> finishedTrips() { return Collections.unmodifiableList(new ArrayList<>(finishedTrips)); }
}

final class Rider extends Account {
    private final PaymentMethod paymentMethod;

    Rider(String id, String name, String phone, PaymentMethod paymentMethod) {
        super(id, name, phone);
        this.paymentMethod = paymentMethod;
    }

    PaymentMethod paymentMethod() { return paymentMethod; }
}

final class Driver extends Account {
    private final Vehicle vehicle;
    private volatile Location location;
    private volatile boolean online;
    private final AtomicBoolean busy = new AtomicBoolean(false);

    Driver(String id, String name, String phone, Vehicle vehicle, Location location) {
        super(id, name, phone);
        this.vehicle = vehicle;
        this.location = location;
    }

    Vehicle vehicle() { return vehicle; }
    Location location() { return location; }
    void moveTo(Location l) { this.location = l; }
    void setOnline(boolean online) { this.online = online; }
    boolean isAvailable() { return online && !busy.get(); }

    /** Atomic claim: of two trips racing for this driver, exactly one wins. */
    boolean tryClaim() { return online && busy.compareAndSet(false, true); }
    void release() { busy.set(false); }
}
