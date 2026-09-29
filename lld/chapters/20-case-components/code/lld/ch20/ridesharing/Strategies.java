package lld.ch20.ridesharing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/* ---------------- Pricing: Strategy + Decorator ---------------- */

/** What a finished trip costs. */
interface PricingStrategy {
    BigDecimal fare(VehicleType type, int distanceKm, int minutes);
}

record RateCard(BigDecimal base, BigDecimal perKm, BigDecimal perMinute, BigDecimal minimum) {}

/** Base + distance + time, with a floor. One rate card per vehicle type. */
final class StandardPricing implements PricingStrategy {
    private final Map<VehicleType, RateCard> cards;

    StandardPricing(Map<VehicleType, RateCard> cards) { this.cards = Map.copyOf(cards); }

    @Override
    public BigDecimal fare(VehicleType type, int km, int minutes) {
        RateCard c = cards.get(type);
        BigDecimal raw = c.base()
                .add(c.perKm().multiply(BigDecimal.valueOf(km)))
                .add(c.perMinute().multiply(BigDecimal.valueOf(minutes)));
        return raw.max(c.minimum()).setScale(2, RoundingMode.HALF_UP);
    }
}

/** Decorator: wraps ANY pricing rule and multiplies its answer. Remove the wrapper to switch surge off. */
final class SurgePricing implements PricingStrategy {
    private final PricingStrategy inner;
    private final BigDecimal multiplier;

    SurgePricing(PricingStrategy inner, BigDecimal multiplier) {
        this.inner = inner;
        this.multiplier = multiplier;
    }

    @Override
    public BigDecimal fare(VehicleType type, int km, int minutes) {
        return inner.fare(type, km, minutes).multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}

/* ---------------- Matching: Strategy ---------------- */

/** Returns a RANKED LIST (not one driver) so a decline just moves to the next name. */
interface MatchingStrategy {
    List<Driver> rank(Location pickup, VehicleType type, Collection<Driver> drivers);
}

final class NearestDriverMatcher implements MatchingStrategy {
    private final int maxRadiusKm;

    NearestDriverMatcher(int maxRadiusKm) { this.maxRadiusKm = maxRadiusKm; }

    @Override
    public List<Driver> rank(Location pickup, VehicleType type, Collection<Driver> drivers) {
        return drivers.stream()
                .filter(Driver::isAvailable)
                .filter(d -> d.vehicle().type() == type)
                .filter(d -> d.location().distanceTo(pickup) <= maxRadiusKm)
                .sorted(Comparator.comparingInt((Driver d) -> d.location().distanceTo(pickup))
                        .thenComparing(Driver::id))            // deterministic tie-break
                .toList();
    }
}

/** A second rule to show the swap: best-rated first, nearest as tie-break. */
final class BestRatedMatcher implements MatchingStrategy {
    private final NearestDriverMatcher nearby;

    BestRatedMatcher(int maxRadiusKm) { this.nearby = new NearestDriverMatcher(maxRadiusKm); }

    @Override
    public List<Driver> rank(Location pickup, VehicleType type, Collection<Driver> drivers) {
        return nearby.rank(pickup, type, drivers).stream()
                .sorted(Comparator.comparing(Driver::rating).reversed()
                        .thenComparingInt(d -> d.location().distanceTo(pickup)))
                .toList();
    }
}

/* ---------------- Payment: Strategy ---------------- */

record Receipt(boolean paid, String detail) {}

interface PaymentMethod {
    Receipt charge(BigDecimal amount);
}

final class CashPayment implements PaymentMethod {
    public Receipt charge(BigDecimal amount) { return new Receipt(true, "collect " + amount + " in cash"); }
}

final class WalletPayment implements PaymentMethod {
    private BigDecimal balance;

    WalletPayment(BigDecimal balance) { this.balance = balance; }

    public synchronized Receipt charge(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) return new Receipt(false, "wallet has only " + balance);
        balance = balance.subtract(amount);
        return new Receipt(true, "wallet debited " + amount + ", left " + balance);
    }
}
