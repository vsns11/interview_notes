package lld.ch08.factorymethod;

import java.util.List;

/** Creator: writes the whole procedure once and leaves one hole - the factory method. */
public abstract class ReportGenerator {

    /** The factory method. Protected: a hook for subclasses, not public API. */
    protected abstract Document createDocument();

    /** The template that USES the factory method. It never names a concrete document. */
    public final String generate() {
        System.out.println("  Collecting rows for the monthly sales report...");
        List<String> rows = List.of("north,120", "south,95", "east,140");
        Document document = createDocument();
        document.render(rows);
        System.out.println("  Report ready: " + document.fileName());
        return document.fileName();
    }
}

final class PdfReportGenerator extends ReportGenerator {
    @Override protected Document createDocument() { return new PdfDocument(); }
}

final class HtmlReportGenerator extends ReportGenerator {
    @Override protected Document createDocument() { return new HtmlDocument(); }
}

final class CsvReportGenerator extends ReportGenerator {
    @Override protected Document createDocument() { return new CsvDocument(); }
}
