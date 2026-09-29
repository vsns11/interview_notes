package lld.ch07.worked.step3;

import java.util.List;

/** Phase 1: pure calculation. Combine Functions into Transform - all derived values in one place. */
public final class StatementCalculator {

    public StatementData calculate(Invoice invoice) {
        List<StatementData.Line> lines = invoice.shipments().stream()
                .map(s -> new StatementData.Line(s.type(), s.weightKg(), s.distanceKm(), amountFor(s)))
                .toList();
        double total = lines.stream().map(StatementData.Line::amount).reduce(0.0, Double::sum);
        int points = invoice.shipments().stream().mapToInt(this::pointsFor).sum();
        return new StatementData(invoice.customer(), lines, total, points);
    }

    double amountFor(Shipment shipment) {
        double result;
        switch (shipment.type()) {
            case "STD":
                result = 5 + shipment.weightKg() * 1.2;
                if (shipment.distanceKm() > 100) result += (shipment.distanceKm() - 100) * 0.05;
                break;
            case "EXP":
                result = 12 + shipment.weightKg() * 2.0;
                if (shipment.distanceKm() > 50) result += (shipment.distanceKm() - 50) * 0.10;
                break;
            case "FRG":
                result = 8 + shipment.weightKg() * 1.5 + 3;
                if (shipment.weightKg() > 10) result = result * 1.2;
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + shipment.type());
        }
        return result;
    }

    int pointsFor(Shipment shipment) {
        int result = shipment.distanceKm() / 100;
        if ("EXP".equals(shipment.type())) result += 2;
        return result;
    }
}
