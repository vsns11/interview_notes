package lld.ch11.templatemethod;

import java.util.ArrayList;
import java.util.List;

/** ABSTRACT CLASS holding the TEMPLATE METHOD. */
public abstract class ImportJob {

    /** Template method. Marked final: subclasses cannot change the order, drop a step or insert one. */
    public final void run(String source) {
        System.out.println("Running the " + name() + " import:");
        List<String> rows = readRows(source);                 // required step
        List<String> records = new ArrayList<>();
        for (String row : rows) records.add(parseRow(row));   // required step
        System.out.println("  parsed " + records.size() + " records: " + records);
        if (shouldValidate()) {                               // hook
            validate(records);
        } else {
            System.out.println("  skipping validation, source is validated upstream");
        }
        save(records);                                        // shared, fixed
        afterSave(records.size());                            // hook with an empty default
        System.out.println("  done");
    }

    // Required steps: the compiler forces every subclass to supply them.
    protected abstract String name();
    protected abstract List<String> readRows(String source);
    protected abstract String parseRow(String row);

    // Hooks: sensible defaults, overriding is optional.
    protected boolean shouldValidate() { return true; }
    protected void afterSave(int count) { }

    // Shared steps: written once, private, cannot be changed by subclasses.
    private void validate(List<String> records) {
        long blank = records.stream().filter(String::isBlank).count();
        System.out.println("  validated " + records.size() + " records, " + blank + " blank");
    }

    private void save(List<String> records) {
        System.out.println("  saved " + records.size() + " records (+ audit entry)");
    }
}
