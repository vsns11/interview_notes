package lld.ch20.logging;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Severity, lowest to highest. A logger set to WARN drops TRACE, DEBUG and INFO. */
enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF;

    boolean isAtLeast(LogLevel threshold) { return this.ordinal() >= threshold.ordinal(); }
}

/** One immutable log event. Created once, passed along the chain, formatted by each appender. */
record LogRecord(Instant time, LogLevel level, String loggerName, String thread, String message, Throwable error) {}

/** Strategy: how a record becomes text. */
interface Formatter {
    String format(LogRecord r);
}

final class PatternFormatter implements Formatter {
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    @Override
    public String format(LogRecord r) {
        String line = String.format("%s %-5s [%s] %s - %s", TIME.format(r.time()), r.level(), r.thread(),
                r.loggerName(), r.message());
        return r.error() == null ? line : line + " | " + r.error();
    }
}

final class JsonFormatter implements Formatter {
    @Override
    public String format(LogRecord r) {
        return "{\"ts\":\"" + r.time() + "\",\"level\":\"" + r.level() + "\",\"logger\":\"" + r.loggerName()
                + "\",\"msg\":\"" + r.message().replace("\"", "\\\"") + "\"}";
    }
}
