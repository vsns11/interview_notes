package lld.ch16.carrental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

enum VehicleType { CAR, TRUCK, SUV, VAN, MOTORCYCLE }

/** Where the vehicle physically is RIGHT NOW. Future availability is answered by booked days, not by this. */
enum VehicleStatus { AVAILABLE, LOANED, BEING_SERVICED, LOST }

enum ReservationStatus { CONFIRMED, ACTIVE, COMPLETED, CANCELLED }

enum BillItemType { BASE_CHARGE, ADD_ON, LATE_FEE }

enum VehicleLogType { RESERVED, PICKED_UP, RETURNED, CANCELLED, FUELING, CLEANING, DAMAGE, REPAIR }

/** Half-open range [start, end): 10 Oct to 13 Oct is 3 rental days and the car is due back on 13 Oct. */
record DateRange(LocalDate start, LocalDate end) {
    DateRange {
        if (!end.isAfter(start)) throw new IllegalArgumentException("end must be after start");
    }
    long days() { return ChronoUnit.DAYS.between(start, end); }
    Stream<LocalDate> dates() { return start.datesUntil(end); }
    @Override public String toString() { return start + ".." + end; }
}

record Member(String id, String name, String email) {}

record Location(String code, String name) {}

record VehicleLogEntry(LocalDate date, VehicleLogType type, String note) {}

record AdditionalDriver(String name, String licenseNumber) {}
