package lld.ch02.access.core;

import java.util.List;

/** The PUBLIC face of the package: exposes only what outsiders are allowed to read. */
public final class Audits {
    private Audits() { }
    public static List<String> all() { return AuditLog.lines(); }
    public static long countFor(Wallet w) { return w.auditCount; }
}
