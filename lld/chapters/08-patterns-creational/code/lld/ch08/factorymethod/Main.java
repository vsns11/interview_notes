package lld.ch08.factorymethod;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // The client picks a creator once, then only talks to ReportGenerator and Document.
        for (ReportGenerator g : List.of(new PdfReportGenerator(), new HtmlReportGenerator(), new CsvReportGenerator())) {
            System.out.println("== " + g.getClass().getSimpleName() + " ==");
            g.generate();
        }

        System.out.println("\n== Supplier-based variant (composition instead of subclassing) ==");
        SupplierReportGenerator gen = new SupplierReportGenerator(HtmlDocument::new);
        System.out.println("  produced " + gen.generate(List.of("west,77")));

        // A test can plug in a fake product with a lambda - no subclass needed.
        SupplierReportGenerator testGen = new SupplierReportGenerator(() -> new Document() {
            public void render(List<String> rows) { System.out.println("  (fake) captured " + rows); }
            public String fileName() { return "fake.txt"; }
        });
        System.out.println("  produced " + testGen.generate(List.of("row-1")));
    }
}
