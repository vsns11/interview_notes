package lld.ch02.access.core;

import java.util.ArrayList;
import java.util.List;

/** Package-private CLASS: an implementation detail of the core package. */
final class AuditLog {
    private static final List<String> LINES = new ArrayList<>();

    private AuditLog() { }

    static void record(Wallet w, String what) {
        w.auditCount++;                          // allowed: same package
        LINES.add(w.owner + ": " + what);
    }

    static List<String> lines() { return List.copyOf(LINES); }
}
