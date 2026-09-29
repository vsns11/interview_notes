package lld.ch10.chain;

/**
 * Logging-level chain. A VARIANT of the pattern: each handler acts if the record
 * is severe enough, and then ALWAYS passes it on, so several handlers may act.
 * (If nobody can stop the flow, it is really a pipeline; the classic form stops.)
 */
abstract class LogHandler {
    private final Level threshold;
    private LogHandler next;

    LogHandler(Level threshold) { this.threshold = threshold; }

    LogHandler setNext(LogHandler next) {
        this.next = next;
        return next;
    }

    final void log(Level level, String message) {
        if (level.compareTo(threshold) >= 0) {
            write(level, message);
        }
        if (next != null) {
            next.log(level, message);
        }
    }

    protected abstract void write(Level level, String message);
}
