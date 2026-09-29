package lld.ch07.worked.step5;

public record FragileTariff() implements Tariff {
    private static final double BASE = 8, PER_KG = 1.5, HANDLING = 3, HEAVY_KG = 10, HEAVY_FACTOR = 1.2;

    @Override
    public double charge(double weightKg, int distanceKm) {
        double base = BASE + weightKg * PER_KG + HANDLING;
        return weightKg > HEAVY_KG ? base * HEAVY_FACTOR : base;
    }
}
