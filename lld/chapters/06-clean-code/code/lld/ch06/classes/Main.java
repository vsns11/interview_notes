package lld.ch06.classes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Classes: small (few responsibilities), cohesive, organised for change, isolated from change. */
public class Main {

    record Sale(String region, long amountPaise) {}

    // ---------------- BEFORE: one class, many reasons to change ----------------
    static class Before {
        static class ReportManager {
            private final List<Sale> sales;
            private String smtpHost = "smtp.example.com";          // email concern
            private String outputFormat = "CSV";                   // formatting concern
            ReportManager(List<Sale> sales) { this.sales = sales; }
            void setOutputFormat(String f) { outputFormat = f; }
            Map<String, Long> totalsByRegion() {                   // business concern
                return sales.stream().collect(Collectors.groupingBy(Sale::region, java.util.TreeMap::new,
                        Collectors.summingLong(Sale::amountPaise)));
            }
            String render() {                                      // grows a new branch for every format
                if (outputFormat.equals("CSV")) {
                    StringBuilder sb = new StringBuilder("region,total\n");
                    totalsByRegion().forEach((r, t) -> sb.append(r).append(',').append(t).append('\n'));
                    return sb.toString().trim();
                } else if (outputFormat.equals("HTML")) {
                    StringBuilder sb = new StringBuilder("<table>");
                    totalsByRegion().forEach((r, t) -> sb.append("<tr><td>").append(r).append("</td><td>").append(t).append("</td></tr>"));
                    return sb.append("</table>").toString();
                }
                throw new IllegalStateException("unknown format");
            }
            String email(String to) { return "SMTP " + smtpHost + " -> " + to + ": " + render(); }
        }
    }

    // ---------------- AFTER ----------------
    static class After {
        /** Cohesive: every method uses the same field (sales). One reason to change: the report's business rules. */
        static final class SalesReport {
            private final List<Sale> sales;
            SalesReport(List<Sale> sales) { this.sales = List.copyOf(sales); }
            Map<String, Long> totalsByRegion() {
                return sales.stream().collect(Collectors.groupingBy(Sale::region, java.util.TreeMap::new,
                        Collectors.summingLong(Sale::amountPaise)));
            }
            long grandTotal() { return sales.stream().mapToLong(Sale::amountPaise).sum(); }
        }

        /** Organised for change: a new format is a NEW class; existing formatters are never reopened (OCP). */
        interface ReportFormatter { String format(SalesReport report); }

        static final class CsvFormatter implements ReportFormatter {
            public String format(SalesReport r) {
                StringBuilder sb = new StringBuilder("region,total\n");
                r.totalsByRegion().forEach((region, total) -> sb.append(region).append(',').append(total).append('\n'));
                return sb.toString().trim();
            }
        }
        static final class HtmlFormatter implements ReportFormatter {
            public String format(SalesReport r) {
                StringBuilder sb = new StringBuilder("<table>");
                r.totalsByRegion().forEach((region, total) ->
                        sb.append("<tr><td>").append(region).append("</td><td>").append(total).append("</td></tr>"));
                return sb.append("</table>").toString();
            }
        }
        static final class JsonFormatter implements ReportFormatter {   // added later, nothing else edited
            public String format(SalesReport r) {
                return r.totalsByRegion().entrySet().stream()
                        .map(e -> "\"" + e.getKey() + "\":" + e.getValue())
                        .collect(Collectors.joining(",", "{", "}"));
            }
        }

        /** Separate reason to change: delivery. */
        static final class ReportMailer {
            private final String smtpHost;
            ReportMailer(String smtpHost) { this.smtpHost = smtpHost; }
            String send(String to, String body) { return "SMTP " + smtpHost + " -> " + to + ": " + body; }
        }

        /** Isolating from change: Portfolio depends on an abstraction, not on a live stock-exchange client. */
        interface PriceSource { long pricePaise(String symbol); }
        static final class Portfolio {
            private final PriceSource prices;
            private final Map<String, Integer> holdings;
            Portfolio(PriceSource prices, Map<String, Integer> holdings) { this.prices = prices; this.holdings = Map.copyOf(holdings); }
            long valuePaise() {
                return holdings.entrySet().stream().mapToLong(h -> prices.pricePaise(h.getKey()) * h.getValue()).sum();
            }
        }
    }

    public static void main(String[] args) {
        List<Sale> sales = List.of(new Sale("North", 5_000), new Sale("South", 7_000), new Sale("North", 1_000));

        System.out.println("== BEFORE ==");
        var manager = new Before.ReportManager(sales);
        System.out.println(manager.render().replace('\n', ' '));
        manager.setOutputFormat("HTML");
        System.out.println(manager.email("boss@example.com"));

        System.out.println("== AFTER ==");
        var report = new After.SalesReport(sales);
        for (After.ReportFormatter f : List.of(new After.CsvFormatter(), new After.HtmlFormatter(), new After.JsonFormatter()))
            System.out.println(f.getClass().getSimpleName() + ": " + f.format(report).replace('\n', ' '));
        System.out.println(new After.ReportMailer("smtp.example.com").send("boss@example.com", new After.JsonFormatter().format(report)));

        After.PriceSource fixedPrices = symbol -> Map.of("INFY", 150_000L, "TCS", 400_000L).get(symbol);  // test-friendly stub
        var portfolio = new After.Portfolio(fixedPrices, Map.of("INFY", 10, "TCS", 2));
        System.out.println("portfolio value (paise) = " + portfolio.valuePaise());
    }
}
