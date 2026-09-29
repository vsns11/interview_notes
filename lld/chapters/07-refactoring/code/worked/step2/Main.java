package lld.ch07.worked.step2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * STEP 2 - Inline Variable (thisAmount), Split Loop, Replace Temp with Query,
 * Replace Loop with Pipeline, Slide Statements.
 * The printing loop now only prints; totals come from their own small queries.
 */
public class Main {

    static class Shipment {
        public String type;
        public double weightKg;
        public int distanceKm;
        Shipment(String type, double weightKg, int distanceKm) {
            this.type = type; this.weightKg = weightKg; this.distanceKm = distanceKm;
        }
    }

    static class Invoice {
        public String customer;
        public List<Shipment> shipments = new ArrayList<>();
    }

    static String statement(Invoice invoice) {
        StringBuilder result = new StringBuilder("Invoice for " + invoice.customer + "\n");
        for (Shipment shipment : invoice.shipments) {
            result.append("  ").append(shipment.type).append(" ").append(shipment.weightKg).append("kg ")
                  .append(shipment.distanceKm).append("km: ").append(money(amountFor(shipment))).append("\n");
        }
        result.append("Amount owed: ").append(money(totalAmount(invoice))).append("\n");
        result.append("Points earned: ").append(totalPoints(invoice)).append("\n");
        return result.toString();
    }

    // reduce(0.0, Double::sum) is a plain left-to-right sum, exactly like the old loop.
    // DoubleStream.sum() may use compensated summation and could differ in the last bit,
    // which would NOT be a pure refactoring.
    static double totalAmount(Invoice invoice) {
        return invoice.shipments.stream().map(Main::amountFor).reduce(0.0, Double::sum);
    }

    static int totalPoints(Invoice invoice) {
        return invoice.shipments.stream().mapToInt(Main::pointsFor).sum();
    }

    static double amountFor(Shipment shipment) {
        double result;
        switch (shipment.type) {
            case "STD":
                result = 5 + shipment.weightKg * 1.2;
                if (shipment.distanceKm > 100) result += (shipment.distanceKm - 100) * 0.05;
                break;
            case "EXP":
                result = 12 + shipment.weightKg * 2.0;
                if (shipment.distanceKm > 50) result += (shipment.distanceKm - 50) * 0.10;
                break;
            case "FRG":
                result = 8 + shipment.weightKg * 1.5 + 3;
                if (shipment.weightKg > 10) result = result * 1.2;
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + shipment.type);
        }
        return result;
    }

    static int pointsFor(Shipment shipment) {
        int result = shipment.distanceKm / 100;
        if ("EXP".equals(shipment.type)) result += 2;
        return result;
    }

    static String money(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }

    public static String run() {
        Invoice invoice = new Invoice();
        invoice.customer = "Asha Traders";
        invoice.shipments.add(new Shipment("STD", 4.0, 250));
        invoice.shipments.add(new Shipment("EXP", 2.5, 80));
        invoice.shipments.add(new Shipment("FRG", 12.0, 40));
        invoice.shipments.add(new Shipment("STD", 1.0, 90));
        invoice.shipments.add(new Shipment("STD", 0.5, 100));
        invoice.shipments.add(new Shipment("FRG", 10.0, 150));
        return statement(invoice);
    }

    public static void main(String[] args) {
        System.out.print(run());
    }
}
