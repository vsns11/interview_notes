package lld.ch20.logging;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        LogManager.getInstance().setClock(() -> Instant.parse("2026-09-28T10:15:30.123Z")); // fixed for a stable demo

        Logger root = LogManager.rootLogger();
        root.setLevel(LogLevel.INFO);
        root.addAppender(new ConsoleAppender(LogLevel.TRACE, new PatternFormatter(), System.out));

        Logger payment = LogManager.getLogger("app.payment");
        Logger app = LogManager.getLogger("app");
        System.out.println("== 1. Same name -> same instance: " + (payment == LogManager.getLogger("app.payment")));

        System.out.println("== 2. Levels: 'app.payment' inherits INFO from root");
        payment.debug("hidden: DEBUG < INFO");
        payment.info("payment 991 authorised");
        payment.warn("retrying gateway call");

        System.out.println("== 3. Raise one subtree to DEBUG; extra JSON sink only for payments");
        MemoryAppender audit = new MemoryAppender(LogLevel.DEBUG, new JsonFormatter());
        payment.setLevel(LogLevel.DEBUG);
        payment.addAppender(audit);
        payment.debug(() -> "card token=" + "tok_" + 42 + " (built lazily)");
        LogManager.getLogger("app.orders").debug("still hidden: app.orders inherits INFO");
        System.out.println("  audit sink got: " + audit.lines());

        System.out.println("== 4. Additivity off: 'app.payment' events stop at its own appenders");
        payment.setAdditive(false);
        payment.error("refund failed", new IllegalStateException("gateway 503"));
        System.out.println("  (nothing printed on console above) audit sink now has " + audit.lines().size() + " lines");
        payment.setAdditive(true);

        System.out.println("== 5. File sink with its own threshold (WARN+)");
        Path file = Files.createTempFile("app", ".log");
        FileAppender fileSink = new FileAppender(LogLevel.WARN, new PatternFormatter(), file);
        app.addAppender(fileSink);
        app.info("info goes to console only");
        app.warn("disk 91% full");
        fileSink.close();
        System.out.println("  file contains: " + Files.readAllLines(file));
        Files.delete(file);

        System.out.println("== 6. Async appender: 4 threads x 1000 events, then close() flushes");
        CountingAppender counter = new CountingAppender();
        AsyncAppender async = new AsyncAppender(counter, 10_000);
        Logger worker = LogManager.getLogger("worker");
        worker.setAdditive(false);
        worker.addAppender(async);
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            int id = t;
            threads.add(Thread.ofPlatform().start(() -> {
                for (int i = 0; i < 1000; i++) worker.info("job " + id + "-" + i);
            }));
        }
        for (Thread t : threads) t.join();
        async.close();
        System.out.println("  written=" + counter.count.get() + " dropped=" + async.dropped()
                + " total=" + (counter.count.get() + async.dropped()));
    }

    static final class CountingAppender implements Appender {
        final AtomicInteger count = new AtomicInteger();
        public void append(LogRecord r) { count.incrementAndGet(); }
    }
}
