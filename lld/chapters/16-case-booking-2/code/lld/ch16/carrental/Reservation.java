package lld.ch16.carrental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** One rental. The rules (add-ons, late fee, freeing days) live here, not in the actors. */
final class Reservation {
    private final String id;
    private final Member member;
    private final Vehicle vehicle;
    private final DateRange period;
    private final Location pickupLocation;
    private final Location returnLocation;
    private final Bill bill = new Bill();
    private final List<ReservationAddOn> addOns = new ArrayList<>();
    private final List<AdditionalDriver> drivers = new ArrayList<>();
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    Reservation(String id, Member member, Vehicle vehicle, DateRange period,
                Location pickup, Location dropoff, BigDecimal baseCharge) {
        this.id = id;
        this.member = member;
        this.vehicle = vehicle;
        this.period = period;
        this.pickupLocation = pickup;
        this.returnLocation = dropoff;
        bill.add(new BillItem("Base: " + vehicle.model() + " x " + period.days() + " days",
                BillItemType.BASE_CHARGE, baseCharge));
    }

    String id() { return id; }
    Member member() { return member; }
    Vehicle vehicle() { return vehicle; }
    DateRange period() { return period; }
    Location pickupLocation() { return pickupLocation; }
    Location returnLocation() { return returnLocation; }
    Bill bill() { return bill; }
    synchronized List<AdditionalDriver> drivers() { return List.copyOf(drivers); }
    synchronized ReservationStatus status() { return status; }

    private void require(ReservationStatus expected, String action) {
        if (status != expected)
            throw new IllegalStateException("cannot " + action + " reservation " + id + " in state " + status);
    }

    synchronized void addAddOn(ReservationAddOn addOn) {
        require(ReservationStatus.CONFIRMED, "add an add-on to");
        addOns.add(addOn);
        bill.add(addOn.toBillItem(period.days()));
    }

    synchronized void addDriver(AdditionalDriver d) {
        require(ReservationStatus.CONFIRMED, "add a driver to");
        drivers.add(d);
    }

    synchronized void pickUp(LocalDate on) {
        require(ReservationStatus.CONFIRMED, "pick up");
        status = ReservationStatus.ACTIVE;
        vehicle.setStatus(VehicleStatus.LOANED);
        vehicle.addLog(on, VehicleLogType.PICKED_UP, id + " by " + member.name() + " at " + pickupLocation.code());
    }

    /** Returns the number of late days charged. The due date is period.end(). */
    synchronized long returnVehicle(LocalDate on, int odometer, LateFeePolicy lateFee) {
        require(ReservationStatus.ACTIVE, "return");
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(period.end(), on));
        if (daysLate > 0) {
            bill.add(new BillItem("Late fee: " + daysLate + " day(s)", BillItemType.LATE_FEE,
                    lateFee.fee(vehicle, daysLate)));
        }
        vehicle.setMileage(odometer);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.release(period);
        vehicle.addLog(on, VehicleLogType.RETURNED, id + " at " + returnLocation.code() + ", odometer " + odometer);
        status = ReservationStatus.COMPLETED;
        return daysLate;
    }

    synchronized void cancel(LocalDate on) {
        require(ReservationStatus.CONFIRMED, "cancel");
        status = ReservationStatus.CANCELLED;
        vehicle.release(period);   // a cancelled rental must give its days back
        vehicle.addLog(on, VehicleLogType.CANCELLED, id);
    }
}
