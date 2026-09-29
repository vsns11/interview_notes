package lld.ch16.carrental;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** One physical vehicle. It owns its booked days and its log. */
final class Vehicle {
    private final String barcode;
    private final String licensePlate;
    private final String make;
    private final String model;
    private final VehicleType type;
    private final int passengerCapacity;
    private final String parkingStall;
    private VehicleStatus status = VehicleStatus.AVAILABLE;
    private int mileage;
    private final Set<LocalDate> bookedDays = new HashSet<>();
    private final List<VehicleLogEntry> log = new ArrayList<>();

    Vehicle(String barcode, String licensePlate, String make, String model,
            VehicleType type, int passengerCapacity, String parkingStall, int mileage) {
        this.barcode = barcode;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.type = type;
        this.passengerCapacity = passengerCapacity;
        this.parkingStall = parkingStall;
        this.mileage = mileage;
    }

    String barcode() { return barcode; }
    String model() { return model; }
    VehicleType type() { return type; }
    String parkingStall() { return parkingStall; }
    synchronized VehicleStatus status() { return status; }
    synchronized int mileage() { return mileage; }
    synchronized void setStatus(VehicleStatus s) { status = s; }
    synchronized void setMileage(int m) {
        if (m < mileage) throw new IllegalArgumentException("odometer cannot go backwards");
        mileage = m;
    }

    synchronized boolean isFreeFor(DateRange r) {
        return r.dates().noneMatch(bookedDays::contains);
    }

    /** Check AND book inside one lock, so two members can never both win the same days. */
    synchronized boolean tryBook(DateRange r) {
        if (!isFreeFor(r)) return false;
        r.dates().forEach(bookedDays::add);
        return true;
    }

    synchronized void release(DateRange r) { r.dates().forEach(bookedDays::remove); }

    synchronized void addLog(LocalDate date, VehicleLogType type, String note) {
        log.add(new VehicleLogEntry(date, type, note));
    }

    synchronized List<VehicleLogEntry> history() { return List.copyOf(log); }

    String describe() {
        return make + " " + model + " (" + type + ", " + passengerCapacity + " seats, plate " + licensePlate + ")";
    }
}
