package lld.ch04.isp.roles;

/** Depends on printing only, so a basic printer is enough. */
public class PrintQueue {
    private final Printer printer;

    public PrintQueue(Printer printer) {
        this.printer = printer;
    }

    public void submit(String doc) {
        printer.printDocument(doc);
    }
}
