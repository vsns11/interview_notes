package lld.ch04.cohesion.after;

import java.util.ArrayList;
import java.util.List;

/** HIGH cohesion: only audit-log work lives here. */
public class AuditFile {
    private final List<String> lines = new ArrayList<>();

    public void writeLine(String line) {
        lines.add(line);
        System.out.println("wrote a line to the audit file: " + line);
    }

    public List<String> readAll() {
        System.out.println("read " + lines.size() + " line(s) back from the audit file");
        return List.copyOf(lines);
    }
}
