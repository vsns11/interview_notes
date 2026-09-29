package lld.ch04.isp.split;

/** Implements only what it can honour. */
public class BasicPrinter implements Printer {
    @Override
    public void printDocument(String doc) {
        System.out.println("BasicPrinter printing " + doc);
    }
}
