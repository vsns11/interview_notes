package lld.ch07.worked.step4;

import java.util.List;

public record StatementData(String customer, List<Line> lines, double totalAmount, int totalPoints) {
    public record Line(ServiceType type, double weightKg, int distanceKm, double amount) { }
}
