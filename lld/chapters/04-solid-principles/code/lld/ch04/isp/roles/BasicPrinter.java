package lld.ch04.isp.roles;

public class BasicPrinter implements Printer {
    @Override
    public void printDocument(String doc) {
        System.out.println("Basic printer printing " + doc);
    }
}
