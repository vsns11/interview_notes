package lld.ch04.isp.roles;

public class OfficeDevice implements DocumentProcessor {
    @Override
    public void printDocument(String doc) {
        System.out.println("Printing " + doc);
    }

    @Override
    public void scanDocument(String doc) {
        System.out.println("Scanning " + doc);
    }

    @Override
    public void stapleDocument(String doc) {
        System.out.println("Stapling " + doc);
    }
}
