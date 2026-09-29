package ch12.realworld;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;
import static java.util.stream.Collectors.*;

/** A realistic reporting pipeline: parse CSV-like lines, validate, aggregate, rank. */
public class SalesReport {
    record Sale(LocalDate date, String region, String product, int units, double unitPrice) {
        double revenue() { return units * unitPrice; }
        static Optional<Sale> parse(String line) {
            String[] f = line.split(",");
            if (f.length != 5) return Optional.empty();
            try {
                return Optional.of(new Sale(LocalDate.parse(f[0].strip()), f[1].strip(), f[2].strip(),
                        Integer.parseInt(f[3].strip()), Double.parseDouble(f[4].strip())));
            } catch (RuntimeException e) { return Optional.empty(); }   // bad number or date -> skip
        }
    }

    public static void main(String[] args) {
        String csv = """
                2025-01-03, EU, keyboard, 10, 49.99
                2025-01-03, US, keyboard, 4, 52.00
                2025-01-04, EU, monitor, 2, 229.00
                bad line
                2025-01-05, US, monitor, 3, 239.00
                2025-02-01, EU, mouse, 25, 19.50
                2025-02-02, APAC, keyboard, x, 45.00
                2025-02-03, APAC, monitor, 5, 210.00
                """;

        List<Sale> sales = csv.lines().map(Sale::parse).flatMap(Optional::stream).toList();
        System.out.println("parsed " + sales.size() + " of " + csv.lines().count() + " lines");

        Map<String, Double> revenueByRegion = sales.stream()
                .collect(groupingBy(Sale::region, TreeMap::new, summingDouble(Sale::revenue)));
        revenueByRegion.forEach((r, v) -> System.out.printf("  %-5s %9.2f%n", r, v));

        Map<Integer, Long> unitsByMonth = sales.stream()
                .collect(groupingBy(s -> s.date().getMonthValue(), TreeMap::new, summingLong(Sale::units)));
        System.out.println("units by month " + unitsByMonth);

        List<String> top2 = sales.stream()
                .collect(groupingBy(Sale::product, summingDouble(Sale::revenue)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(2)
                .map(e -> e.getKey() + "=" + String.format("%.2f", e.getValue()))
                .toList();
        System.out.println("top-2 products by revenue " + top2);

        Map<Boolean, Long> bigOrders = sales.stream().collect(partitioningBy(s -> s.revenue() >= 500, counting()));
        System.out.println("orders >= 500: " + bigOrders.get(true) + ", smaller: " + bigOrders.get(false));
    }
}
