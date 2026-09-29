package lld.ch01.parkinglot;

import java.math.BigDecimal;

/** A NEW rule added later: flat $10.00 per visit. Added as a class; no existing code was edited (open-closed). */
public final class FlatWeekendRate implements PricingStrategy {
    private static final BigDecimal FLAT = new BigDecimal("10.00");

    @Override public BigDecimal fee(VehicleType type, long minutesParked) { return FLAT; }
}
