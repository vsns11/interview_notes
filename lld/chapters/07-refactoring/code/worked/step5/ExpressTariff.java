package lld.ch07.worked.step5;

public record ExpressTariff() implements Tariff {
    private static final double BASE = 12, PER_KG = 2.0, FREE_KM = 50, PER_EXTRA_KM = 0.10;
    private static final int BONUS_POINTS = 2;

    @Override
    public double charge(double weightKg, int distanceKm) {
        return BASE + weightKg * PER_KG + (distanceKm > FREE_KM ? (distanceKm - FREE_KM) * PER_EXTRA_KM : 0);
    }

    @Override
    public int loyaltyPoints(int distanceKm) {
        return Tariff.super.loyaltyPoints(distanceKm) + BONUS_POINTS;
    }
}
