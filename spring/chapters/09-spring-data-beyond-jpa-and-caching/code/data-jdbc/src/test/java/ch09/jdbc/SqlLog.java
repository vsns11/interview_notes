package ch09.jdbc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/** Captures the SQL that Spring's JdbcTemplate logs at DEBUG, so tests can print and count it. */
final class SqlLog {
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    SqlLog() {
        Logger logger = (Logger) LoggerFactory.getLogger("org.springframework.jdbc.core.JdbcTemplate");
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);                  // capture only, do not also print every line
        appender.start();
        logger.addAppender(appender);
    }

    void clear() { appender.list.clear(); }

    /**
     * Only the statements, without JdbcTemplate's wording. Made shorter for printing: quotes removed
     * and TABLE.COL AS COL shown as COL. A JDBC batch is logged once, marked [batch].
     */
    List<String> statements() {
        List<String> out = new ArrayList<>();
        for (ILoggingEvent e : List.copyOf(appender.list)) {
            String m = e.getFormattedMessage();
            if (!m.contains("[")) continue;
            String sql = m.substring(m.indexOf('[') + 1, m.lastIndexOf(']'))
                    .replace("\"", "")
                    .replaceAll("\\w+\\.(\\w+) AS \\1", "$1");
            if (m.startsWith("Executing SQL batch update")) out.add("[batch] " + sql);
            else if (out.isEmpty() || !out.getLast().equals("[batch] " + sql)) out.add(sql);
        }
        return out;
    }

    void print(String title) {
        System.out.println("-- " + title);
        statements().forEach(s -> System.out.println("   " + s));
    }
}
