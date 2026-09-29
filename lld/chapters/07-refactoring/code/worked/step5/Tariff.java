package lld.ch07.worked.step5;

/**
 * Replace Conditional with Polymorphism: each branch of the old switch became a class.
 * A sealed interface lists every permitted implementation, so the compiler knows the full set.
 */
public sealed interface Tariff permits StandardTariff, ExpressTariff, FragileTariff {

    double charge(double weightKg, int distanceKm);

    /** Default behaviour shared by most tariffs; ExpressTariff overrides it. */
    default int loyaltyPoints(int distanceKm) {
        return distanceKm / 100;
    }
}
