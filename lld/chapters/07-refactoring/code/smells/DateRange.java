package lld.ch07.smells;

import java.time.LocalDate;

/** Cure for Data Clumps: start and end always travelled together, so they become one object. */
public record DateRange(LocalDate start, LocalDate end) {

    public DateRange {
        if (end.isBefore(start)) throw new IllegalArgumentException("end before start");
    }

    public boolean contains(LocalDate day) {
        return !day.isBefore(start) && !day.isAfter(end);
    }

    public boolean overlaps(DateRange other) {
        return !other.end.isBefore(start) && !other.start.isAfter(end);
    }
}
