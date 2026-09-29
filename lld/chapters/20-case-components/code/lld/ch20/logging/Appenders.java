package lld.ch20.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/** A sink (destination). Each has its own threshold and formatter. */
interface Appender extends AutoCloseable {
    void append(LogRecord r);

    @Override
    default void close() {}
}

/** Shared threshold + formatter handling, so concrete appenders only write text. */
abstract class AbstractAppender implements Appender {
    private final LogLevel threshold;
    protected final Formatter formatter;

    AbstractAppender(LogLevel threshold, Formatter formatter) {
        this.threshold = threshold;
        this.formatter = formatter;
    }

    @Override
    public final void append(LogRecord r) {
        if (r.level().isAtLeast(threshold)) write(formatter.format(r));
    }

    protected abstract void write(String line);
}

final class ConsoleAppender extends AbstractAppender {
    private final PrintStream out;

    ConsoleAppender(LogLevel threshold, Formatter f, PrintStream out) {
        super(threshold, f);
        this.out = out;
    }

    @Override
    protected synchronized void write(String line) { out.println(line); }
}

final class FileAppender extends AbstractAppender {
    private final BufferedWriter writer;

    FileAppender(LogLevel threshold, Formatter f, Path file) throws IOException {
        super(threshold, f);
        this.writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    @Override
    protected synchronized void write(String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

/** Keeps lines in memory - handy for tests. */
final class MemoryAppender extends AbstractAppender {
    private final List<String> lines = new ArrayList<>();

    MemoryAppender(LogLevel threshold, Formatter f) { super(threshold, f); }

    @Override
    protected synchronized void write(String line) { lines.add(line); }

    synchronized List<String> lines() { return List.copyOf(lines); }
}

/**
 * Decorator: makes ANY appender asynchronous. Callers only enqueue; one background thread writes.
 * Bounded queue: when full we drop and count (a common production choice), never block the app.
 */
final class AsyncAppender implements Appender {
    private static final LogRecord POISON = new LogRecord(null, LogLevel.OFF, "", "", "", null);
    private final Appender delegate;
    private final BlockingQueue<LogRecord> queue;
    private final Thread worker;
    private final AtomicLong dropped = new AtomicLong();
    private volatile boolean closed;

    AsyncAppender(Appender delegate, int capacity) {
        this.delegate = delegate;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.worker = Thread.ofPlatform().daemon().name("async-log-writer").start(this::drain);
    }

    @Override
    public void append(LogRecord r) {
        if (closed || !queue.offer(r)) dropped.incrementAndGet(); // full or closed: count it, never block
    }

    private void drain() {
        try {
            for (LogRecord r = queue.take(); r != POISON; r = queue.take()) {
                try {
                    delegate.append(r);
                } catch (RuntimeException e) {  // one bad write must not kill the writer thread
                    System.err.println("async appender: delegate failed: " + e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    long dropped() { return dropped.get(); }

    /** Flush everything already queued, then stop. Records appended after this point are counted as dropped. */
    @Override
    public void close() {
        if (closed) return;
        closed = true;
        try {
            queue.put(POISON);
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        delegate.close();
    }
}
