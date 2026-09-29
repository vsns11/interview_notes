package lld.ch11.templatemethod;

import java.util.Arrays;
import java.util.List;

/** Supplies only what differs for CSV. */
public final class CsvImportJob extends ImportJob {
    @Override protected String name() { return "CSV"; }

    @Override
    protected List<String> readRows(String source) {
        return Arrays.stream(source.split("\n")).skip(1).toList();   // skip the header line
    }

    @Override
    protected String parseRow(String row) { return row.split(",")[0].trim(); }
}
