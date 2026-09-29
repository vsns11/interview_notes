package lld.ch07.worked.step3;

import java.util.Locale;

/** Phase 2: pure formatting. Knows nothing about tariffs. */
public final class TextRenderer {

    public String render(StatementData data) {
        StringBuilder result = new StringBuilder("Invoice for " + data.customer() + "\n");
        for (StatementData.Line line : data.lines()) {
            result.append("  ").append(line.type()).append(" ").append(line.weightKg()).append("kg ")
                  .append(line.distanceKm()).append("km: ").append(money(line.amount())).append("\n");
        }
        result.append("Amount owed: ").append(money(data.totalAmount())).append("\n");
        result.append("Points earned: ").append(data.totalPoints()).append("\n");
        return result.toString();
    }

    static String money(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }
}
