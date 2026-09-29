package lld.ch11.templatemethod;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Supplies what differs for JSON, and overrides two hooks. (Toy parsing: no JSON library.) */
public final class JsonImportJob extends ImportJob {
    private static final Pattern OBJECT = Pattern.compile("\\{[^}]*}");
    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]*)\"");

    @Override protected String name() { return "JSON"; }

    @Override
    protected List<String> readRows(String source) {
        return OBJECT.matcher(source).results().map(m -> m.group()).toList();
    }

    @Override
    protected String parseRow(String row) {
        Matcher m = ID.matcher(row);
        return m.find() ? m.group(1) : "";
    }

    @Override protected boolean shouldValidate() { return false; }

    @Override
    protected void afterSave(int count) { System.out.println("  [hook] notified upstream: " + count + " ids consumed"); }
}
