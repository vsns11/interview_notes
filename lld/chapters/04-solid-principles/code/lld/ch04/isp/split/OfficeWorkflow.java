package lld.ch04.isp.split;

/** OVER-SPLIT: needs all three, so it takes three dependencies -- and gains nothing. */
public class OfficeWorkflow {
    private final Printer printer;
    private final Scanner scanner;
    private final Stapler stapler;

    public OfficeWorkflow(Printer printer, Scanner scanner, Stapler stapler) {
        this.printer = printer;
        this.scanner = scanner;
        this.stapler = stapler;
    }

    public void run(String doc) {
        scanner.scanDocument(doc);
        printer.printDocument(doc);
        stapler.stapleDocument(doc);
    }
}
