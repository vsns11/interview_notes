package lld.ch01.parkinglot;

import java.math.BigDecimal;

/** $4.00 for the first hour, $3.50 for hours 2 and 3, $2.50 per hour after that. Part hours round up. Trucks pay double. */
public final class HourlyRate implements PricingStrategy {
    private static final BigDecimal FIRST_HOUR = new BigDecimal("4.00");
    private static final BigDecimal HOURS_2_TO_3 = new BigDecimal("3.50");
    private static final BigDecimal LATER_HOURS = new BigDecimal("2.50");

    @Override
    public BigDecimal fee(VehicleType type, long minutesParked) {
        long hours = Math.max(1, (minutesParked + 59) / 60);    // ceiling, minimum one hour
        BigDecimal total = BigDecimal.ZERO;
        for (long h = 1; h <= hours; h++) {
            total = total.add(h == 1 ? FIRST_HOUR : (h <= 3 ? HOURS_2_TO_3 : LATER_HOURS));
        }
        return type == VehicleType.TRUCK ? total.multiply(BigDecimal.TWO) : total;
    }
}
