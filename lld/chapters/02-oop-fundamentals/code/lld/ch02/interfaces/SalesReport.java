package lld.ch02.interfaces;

public class SalesReport extends Report {
    private final long totalCents;
    public SalesReport(long totalCents) { super("Sales"); this.totalCents = totalCents; }
    @Override protected String body() { return "total: " + totalCents + " cents"; }
}
