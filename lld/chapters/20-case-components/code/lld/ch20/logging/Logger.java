package lld.ch20.logging;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * A named logger in a dot-separated hierarchy ("app.payment" -> "app" -> root).
 * An event is handed to this logger's appenders, then passed UP to the parent's appenders
 * (Chain of Responsibility) until a logger with additivity=false stops it.
 */
final class Logger {
    private final String name;
    private final Logger parent;
    private final LogManager manager;
    private volatile LogLevel level;                     // null = inherit from the nearest ancestor
    private volatile boolean additive = true;
    private final List<Appender> appenders = new CopyOnWriteArrayList<>();

    Logger(String name, Logger parent, LogManager manager) {
        this.name = name;
        this.parent = parent;
        this.manager = manager;
    }

    String name() { return name; }
    void setLevel(LogLevel level) { this.level = level; }
    void setAdditive(boolean additive) { this.additive = additive; }
    void addAppender(Appender a) { appenders.add(a); }

    LogLevel effectiveLevel() {
        for (Logger l = this; l != null; l = l.parent) if (l.level != null) return l.level;
        return LogLevel.INFO;
    }

    boolean isEnabled(LogLevel lvl) { return lvl != LogLevel.OFF && lvl.isAtLeast(effectiveLevel()); }

    void trace(String msg) { log(LogLevel.TRACE, msg, null); }
    void debug(String msg) { log(LogLevel.DEBUG, msg, null); }
    void info(String msg) { log(LogLevel.INFO, msg, null); }
    void warn(String msg) { log(LogLevel.WARN, msg, null); }
    void error(String msg, Throwable t) { log(LogLevel.ERROR, msg, t); }

    /** Lazy message: the (possibly expensive) string is only built if the level is enabled. */
    void debug(Supplier<String> msg) { if (isEnabled(LogLevel.DEBUG)) log(LogLevel.DEBUG, msg.get(), null); }

    void log(LogLevel lvl, String msg, Throwable t) {
        if (!isEnabled(lvl)) return;                     // cheap check first
        LogRecord r = new LogRecord(manager.now(), lvl, name, Thread.currentThread().getName(), msg, t);
        for (Logger l = this; l != null; l = l.parent) { // the chain
            for (Appender a : l.appenders) {
                try {
                    a.append(r);
                } catch (RuntimeException e) {
                    System.err.println("appender failed: " + e); // logging must never crash the app
                }
            }
            if (!l.additive) break;
        }
    }
}

/** Singleton registry of loggers (initialization-on-demand holder idiom: lazy and thread-safe). */
final class LogManager {
    private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private final Logger root = new Logger("root", null, this);
    private volatile Supplier<Instant> clock = Instant::now;

    private LogManager() { root.setLevel(LogLevel.INFO); }

    private static final class Holder { static final LogManager INSTANCE = new LogManager(); }

    static LogManager getInstance() { return Holder.INSTANCE; }

    /** Factory method: same name -> same Logger instance; parents are created first. */
    static Logger getLogger(String name) { return getInstance().loggerFor(name); }

    static Logger rootLogger() { return getInstance().root; }

    private Logger loggerFor(String name) {
        if (name == null || name.isEmpty() || name.equals("root")) return root;
        Logger existing = loggers.get(name);
        if (existing != null) return existing;
        int dot = name.lastIndexOf('.');
        Logger parent = dot < 0 ? root : loggerFor(name.substring(0, dot)); // resolved OUTSIDE computeIfAbsent
        return loggers.computeIfAbsent(name, n -> new Logger(n, parent, this));
    }

    Instant now() { return clock.get(); }
    void setClock(Supplier<Instant> c) { this.clock = c; }
}
