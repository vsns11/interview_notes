package lld.ch04.isp.roles;

/** Role interface for callers that only print (e.g. PrintQueue). */
public interface Printer {
    void printDocument(String doc);
}
