package lld.ch07.worked.step4;

import java.util.List;

/** Replace Magic Literal: every tariff number now has a name. The switch is exhaustive over the enum. */
public final class StatementCalculator {

    static final double STD_BASE = 5, STD_PER_KG = 1.2, STD_FREE_KM = 100, STD_PER_EXTRA_KM = 0.05;
    static final double EXP_BASE = 12, EXP_PER_KG = 2.0, EXP_FREE_KM = 50, EXP_PER_EXTRA_KM = 0.10;
    static final double FRG_BASE = 8, FRG_PER_KG = 1.5, FRG_HANDLING = 3, FRG_HEAVY_KG = 10, FRG_HEAVY_FACTOR = 1.2;
    static final int KM_PER_POINT = 100, EXPRESS_BONUS_POINTS = 2;

    public StatementData calculate(Invoice invoice) {
        List<StatementData.Line> lines = invoice.shipments().stream()
                .map(s -> new StatementData.Line(s.type(), s.weightKg(), s.distanceKm(), amountFor(s)))
                .toList();
        double total = lines.stream().map(StatementData.Line::amount).reduce(0.0, Double::sum);
        int points = invoice.shipments().stream().mapToInt(this::pointsFor).sum();
        return new StatementData(invoice.customer(), lines, total, points);
    }

    double amountFor(Shipment s) {
        return switch (s.type()) {
            case STANDARD -> STD_BASE + s.weightKg() * STD_PER_KG
                    + (s.distanceKm() > STD_FREE_KM ? (s.distanceKm() - STD_FREE_KM) * STD_PER_EXTRA_KM : 0);
            case EXPRESS -> EXP_BASE + s.weightKg() * EXP_PER_KG
                    + (s.distanceKm() > EXP_FREE_KM ? (s.distanceKm() - EXP_FREE_KM) * EXP_PER_EXTRA_KM : 0);
            case FRAGILE -> {
                double base = FRG_BASE + s.weightKg() * FRG_PER_KG + FRG_HANDLING;
                yield s.weightKg() > FRG_HEAVY_KG ? base * FRG_HEAVY_FACTOR : base;
            }
        };
    }

    int pointsFor(Shipment s) {
        int result = s.distanceKm() / KM_PER_POINT;
        if (s.type() == ServiceType.EXPRESS) result += EXPRESS_BONUS_POINTS;
        return result;
    }
}
