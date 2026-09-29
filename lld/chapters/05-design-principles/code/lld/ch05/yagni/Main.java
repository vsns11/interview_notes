package lld.ch05.yagni;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** YAGNI: build what the current requirement needs ("export orders as CSV"), not what might be asked one day. */
public class Main {

    record Order(String id, String customer, BigDecimal total) {}   // money as BigDecimal, never double

    // ---------------- BEFORE: speculative generality ----------------
    static class Before {
        enum Format { CSV, PDF, XML, EXCEL, YAML }

        /** Five formats, pluggable encodings, compression... only CSV was requested. */
        interface ReportExporter {
            String export(List<Order> orders, Format format, String encoding, boolean compress);
        }

        static class UniversalExporter implements ReportExporter {
            @Override
            public String export(List<Order> orders, Format format, String encoding, boolean compress) {
                if (compress) throw new UnsupportedOperationException("compression not implemented yet");
                return switch (format) {
                    case CSV -> orders.stream().map(o -> o.id() + "," + o.customer() + "," + o.total().toPlainString())
                            .collect(Collectors.joining("\n", "id,customer,total\n", ""));
                    case PDF, XML, EXCEL, YAML -> throw new UnsupportedOperationException(format + " not implemented yet");
                };
            }
        }
    }

    // ---------------- AFTER: exactly what is needed today ----------------
    static class After {
        static String toCsv(List<Order> orders) {
            return orders.stream()
                    .map(o -> String.join(",", o.id(), o.customer(), o.total().toPlainString()))
                    .collect(Collectors.joining("\n", "id,customer,total\n", ""));
        }
    }

    public static void main(String[] args) {
        List<Order> orders = List.of(new Order("o1", "asha", new BigDecimal("250.00")),
                                     new Order("o2", "ravi", new BigDecimal("99.50")));
        System.out.println("== BEFORE ==");
        var exporter = new Before.UniversalExporter();
        System.out.println(exporter.export(orders, Before.Format.CSV, "UTF-8", false));
        try {
            exporter.export(orders, Before.Format.PDF, "UTF-8", false);
        } catch (UnsupportedOperationException e) {
            System.out.println("dead option: " + e.getMessage());
        }
        System.out.println("== AFTER ==");
        System.out.println(After.toCsv(orders));
    }
}
