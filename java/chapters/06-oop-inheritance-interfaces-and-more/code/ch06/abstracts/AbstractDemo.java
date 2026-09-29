package ch06.abstracts;

public class AbstractDemo {

    // Abstract class: shared state + shared algorithm, with steps left for subclasses.
    static abstract class Report {
        private final String title;
        protected Report(String title) { this.title = title; }   // abstract classes still have constructors

        // Template method: the fixed skeleton. final so subclasses cannot change the order.
        public final String render() {
            return header() + "\n" + body() + "\n" + footer();
        }
        protected String header() { return "== " + title + " =="; }   // default step, may be overridden
        protected abstract String body();                             // required step: no body here
        protected String footer() { return "-- end --"; }
    }

    static class SalesReport extends Report {
        private final int units;
        SalesReport(int units) { super("Sales"); this.units = units; }
        @Override protected String body() { return "units sold: " + units; }
    }

    static class AuditReport extends Report {
        AuditReport() { super("Audit"); }
        @Override protected String body() { return "no issues found"; }
        @Override protected String footer() { return "-- signed: auditor --"; }
    }

    public static void main(String[] args) {
        // new Report("x");   -> compile error: Report is abstract; cannot be instantiated
        Report[] reports = { new SalesReport(42), new AuditReport() };
        for (Report r : reports) System.out.println(r.render());

        Report adHoc = new Report("Ad hoc") {                 // anonymous subclass supplies body()
            @Override protected String body() { return "one-off content"; }
        };
        System.out.println(adHoc.render());
    }
}
