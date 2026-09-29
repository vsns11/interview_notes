package lld.ch01.parkinglot;

import java.math.BigDecimal;

/** Strategy: turns a stay into an amount of money. Money is BigDecimal, never double (no binary rounding errors). */
public interface PricingStrategy {
    BigDecimal fee(VehicleType type, long minutesParked);
}
