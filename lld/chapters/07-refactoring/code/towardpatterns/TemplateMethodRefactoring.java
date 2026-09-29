package lld.ch07.towardpatterns;

import java.util.List;

/** Form Template Method: two siblings with the same algorithm skeleton but different details. */
public final class TemplateMethodRefactoring {

    // ---------- BEFORE: duplicated skeleton ----------
    static String legacyCsv(List<String> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("name\n");                                   // header
        for (String r : rows) sb.append(r).append("\n");       // body
        sb.append("# ").append(rows.size()).append(" rows");   // footer
        return sb.toString();
    }
    static String legacyMarkdown(List<String> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("| name |\n|---|\n");
        for (String r : rows) sb.append("| ").append(r).append(" |\n");
        sb.append("_").append(rows.size()).append(" rows_");
        return sb.toString();
    }

    // ---------- AFTER: the skeleton lives once, subclasses fill in the steps ----------
    abstract static class Exporter {
        /** The template method - final so subclasses cannot change the order of steps. */
        final String export(List<String> rows) {
            StringBuilder sb = new StringBuilder(header());
            for (String r : rows) sb.append(row(r));
            return sb.append(footer(rows.size())).toString();
        }
        protected abstract String header();
        protected abstract String row(String value);
        protected abstract String footer(int count);
    }
    static final class CsvExporter extends Exporter {
        protected String header() { return "name\n"; }
        protected String row(String v) { return v + "\n"; }
        protected String footer(int n) { return "# " + n + " rows"; }
    }
    static final class MarkdownExporter extends Exporter {
        protected String header() { return "| name |\n|---|\n"; }
        protected String row(String v) { return "| " + v + " |\n"; }
        protected String footer(int n) { return "_" + n + " rows_"; }
    }

    static boolean demo() {
        List<String> rows = List.of("Asha", "Ravi");
        boolean same = legacyCsv(rows).equals(new CsvExporter().export(rows))
                && legacyMarkdown(rows).equals(new MarkdownExporter().export(rows));
        System.out.println("Template Method: CSV and Markdown exporters | same: " + same);
        return same;
    }
}
