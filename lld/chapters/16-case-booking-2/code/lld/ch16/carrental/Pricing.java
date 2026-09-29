package lld.ch16.carrental;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/** Strategy: how the base charge for a rental is computed. */
interface PricingStrategy { BigDecimal baseCharge(Vehicle v, DateRange period); }

final class DailyRatePricing implements PricingStrategy {
    private final Map<VehicleType, BigDecimal> dailyRate;
    DailyRatePricing(Map<VehicleType, BigDecimal> dailyRate) { this.dailyRate = new EnumMap<>(dailyRate); }
    public BigDecimal baseCharge(Vehicle v, DateRange p) {
        return dailyRate.get(v.type()).multiply(BigDecimal.valueOf(p.days()));
    }
}

/** Decorator over any pricing: 10% off rentals of 7 days or more. */
final class WeeklyDiscountPricing implements PricingStrategy {
    private final PricingStrategy inner;
    WeeklyDiscountPricing(PricingStrategy inner) { this.inner = inner; }
    public BigDecimal baseCharge(Vehicle v, DateRange p) {
        BigDecimal base = inner.baseCharge(v, p);
        return p.days() >= 7 ? base.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP) : base;
    }
}

/** Strategy: what a late return costs. */
interface LateFeePolicy { BigDecimal fee(Vehicle v, long daysLate); }

record FlatLateFee(BigDecimal perDay) implements LateFeePolicy {
    public BigDecimal fee(Vehicle v, long daysLate) { return perDay.multiply(BigDecimal.valueOf(daysLate)); }
}

/** Observer-style port: the service announces events, the notifier decides the channel. */
interface Notifier { void send(Member to, String message); }

final class ConsoleNotifier implements Notifier {
    public void send(Member to, String message) { System.out.println("    [notify " + to.email() + "] " + message); }
}
