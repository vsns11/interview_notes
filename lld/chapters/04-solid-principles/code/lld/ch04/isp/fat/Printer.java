package lld.ch04.isp.fat;

/** A FAT interface: one list of everything any printer might ever do. */
public interface Printer {
    void printDocument(String doc);

    void scanDocument(String doc);

    void faxDocument(String doc);

    void stapleDocument(String doc);
}
