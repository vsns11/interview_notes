package lld.ch08.factorymethod;

import java.util.List;

/** Product: the only document type ReportGenerator knows about. */
public interface Document {
    void render(List<String> rows);
    String fileName();
}

final class PdfDocument implements Document {
    public void render(List<String> rows) { System.out.println("  Rendering as PDF: " + rows.size() + " rows"); }
    public String fileName() { return "sales.pdf"; }
}

final class HtmlDocument implements Document {
    public void render(List<String> rows) { System.out.println("  Rendering as HTML table: " + rows.size() + " rows"); }
    public String fileName() { return "sales.html"; }
}

/** Added later - no existing class had to change. */
final class CsvDocument implements Document {
    public void render(List<String> rows) { rows.forEach(r -> System.out.println("  csv> " + r)); }
    public String fileName() { return "sales.csv"; }
}
