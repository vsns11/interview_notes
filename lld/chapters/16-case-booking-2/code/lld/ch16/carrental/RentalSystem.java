package lld.ch16.carrental;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Per-branch indexes over vehicles. All three are filled by add(), so they cannot drift apart. */
final class VehicleInventory {
    private final Map<String, Vehicle> byBarcode = new ConcurrentHashMap<>();
    private final Map<VehicleType, List<Vehicle>> byType = new ConcurrentHashMap<>();
    private final Map<String, List<Vehicle>> byModel = new ConcurrentHashMap<>();

    void add(Vehicle v) {
        if (byBarcode.putIfAbsent(v.barcode(), v) != null)
            throw new IllegalArgumentException("duplicate barcode " + v.barcode());
        byType.computeIfAbsent(v.type(), t -> new CopyOnWriteArrayList<>()).add(v);
        byModel.computeIfAbsent(v.model(), m -> new CopyOnWriteArrayList<>()).add(v);
    }

    Optional<Vehicle> scan(String barcode) { return Optional.ofNullable(byBarcode.get(barcode)); }

    List<Vehicle> search(VehicleType type, DateRange period) {
        return byType.getOrDefault(type, List.of()).stream()
                .filter(v -> v.status() != VehicleStatus.LOST && v.isFreeFor(period)).toList();
    }

    List<Vehicle> searchByModel(String model, DateRange period) {
        return byModel.getOrDefault(model, List.of()).stream().filter(v -> v.isFreeFor(period)).toList();
    }
}

record CarRentalLocation(Location location, VehicleInventory inventory) {}

/** Creates and tracks reservations. Answers "what did this member rent" and "who took this car". */
final class ReservationService {
    private final PricingStrategy pricing;
    private final LateFeePolicy lateFee;
    private final Notifier notifier;
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1000);

    ReservationService(PricingStrategy pricing, LateFeePolicy lateFee, Notifier notifier) {
        this.pricing = pricing;
        this.lateFee = lateFee;
        this.notifier = notifier;
    }

    Optional<Reservation> reserve(Member m, Vehicle v, DateRange period, Location pickup, Location dropoff, LocalDate today) {
        if (!v.tryBook(period)) return Optional.empty();
        var r = new Reservation("R" + seq.incrementAndGet(), m, v, period, pickup, dropoff, pricing.baseCharge(v, period));
        reservations.put(r.id(), r);
        v.addLog(today, VehicleLogType.RESERVED, r.id() + " for " + period);
        notifier.send(m, "Reservation " + r.id() + " confirmed: " + v.model() + " " + period);
        return Optional.of(r);
    }

    Reservation get(String id) {
        var r = reservations.get(id);
        if (r == null) throw new IllegalArgumentException("no reservation " + id);
        return r;
    }

    void cancel(String id, LocalDate on) {
        var r = get(id);
        r.cancel(on);
        notifier.send(r.member(), "Reservation " + id + " cancelled");
    }

    void pickUp(String id, LocalDate on) { get(id).pickUp(on); }

    void returnVehicle(String id, LocalDate on, int odometer) {
        var r = get(id);
        long late = r.returnVehicle(on, odometer, lateFee);
        if (late > 0) notifier.send(r.member(), "Returned " + late + " day(s) late, late fee added to " + id);
    }

    List<Reservation> byMember(String memberId) {
        return reservations.values().stream().filter(r -> r.member().id().equals(memberId)).toList();
    }

    List<Reservation> byVehicle(String barcode) {
        return reservations.values().stream().filter(r -> r.vehicle().barcode().equals(barcode)).toList();
    }

    /** Requirement: remind before pick-up, before the due date, and when overdue. Run once a day by a scheduler. */
    void sendReminders(LocalDate today) {
        for (var r : reservations.values()) {
            var status = r.status();
            if (status == ReservationStatus.CONFIRMED && r.period().start().equals(today.plusDays(1)))
                notifier.send(r.member(), "Pick-up tomorrow for " + r.id() + " at " + r.pickupLocation().name());
            else if (status == ReservationStatus.ACTIVE && r.period().end().equals(today.plusDays(1)))
                notifier.send(r.member(), "Vehicle for " + r.id() + " is due tomorrow");
            else if (status == ReservationStatus.ACTIVE && r.period().end().isBefore(today))
                notifier.send(r.member(), "OVERDUE: " + r.id() + " was due " + r.period().end());
        }
    }
}

/** Top-level object: owns branches (each owning its inventory) and the reservation service. */
final class CarRentalSystem {
    private final Map<String, CarRentalLocation> locations = new ConcurrentHashMap<>();
    private final ReservationService reservations;

    CarRentalSystem(ReservationService reservations) { this.reservations = reservations; }

    CarRentalLocation addLocation(Location l) {
        return locations.computeIfAbsent(l.code(), c -> new CarRentalLocation(l, new VehicleInventory()));
    }
    CarRentalLocation location(String code) { return locations.get(code); }
    ReservationService reservations() { return reservations; }

    List<Vehicle> searchAll(VehicleType type, DateRange period) {
        List<Vehicle> out = new ArrayList<>();
        locations.values().forEach(l -> out.addAll(l.inventory().search(type, period)));
        return out;
    }
}
