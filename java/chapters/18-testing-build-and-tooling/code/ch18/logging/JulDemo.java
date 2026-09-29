package ch18.logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/** java.util.logging (JUL): configuration, levels, lazy messages, exceptions, logger hierarchy, System.Logger. */
public class JulDemo {

    // What you would normally put in a logging.properties file (-Djava.util.logging.config.file=...)
    static final String CONFIG = """
            handlers = java.util.logging.ConsoleHandler
            .level = INFO
            java.util.logging.ConsoleHandler.level = ALL
            java.util.logging.SimpleFormatter.format = %4$-7s [%3$s] %5$s%6$s%n
            ch18.logging.level = FINE
            """;

    public static void main(String[] args) throws IOException {
        System.setErr(System.out);                        // ConsoleHandler writes to System.err; merge for the demo
        LogManager.getLogManager().readConfiguration(
                new ByteArrayInputStream(CONFIG.getBytes(StandardCharsets.UTF_8)));
        // Normally a static final field; created AFTER the configuration here because the demo configures in code.
        Logger log = Logger.getLogger(JulDemo.class.getName());                     // "ch18.logging.JulDemo"

        System.out.println("-- 1. levels (ch18.logging is set to FINE, the root to INFO)");
        log.severe("disk full");
        log.warning("slow response: 1200 ms");
        log.info("server started on port 8080");
        log.config("pool size = 10");
        log.fine("cache miss for key 42");
        log.finer("not shown: FINER is below FINE");

        System.out.println("-- 2. another package inherits the root level INFO");
        Logger other = Logger.getLogger("com.acme.billing");
        other.info("billing ready");
        other.fine("not shown: com.acme.billing is at INFO");

        System.out.println("-- 3. parameters and lazy messages");
        log.log(Level.INFO, "user {0} bought {1} items", new Object[]{"ann", 3});   // MessageFormat style
        int[] calls = {0};
        Logger quiet = Logger.getLogger("com.acme.quiet");
        quiet.fine(() -> "expensive " + (++calls[0]));   // Supplier: not evaluated, FINE is disabled here
        quiet.fine("eager " + (++calls[0]));               // string built anyway, then thrown away
        System.out.println("message expressions evaluated: " + calls[0]);
        System.out.println("isLoggable(FINE) = " + quiet.isLoggable(Level.FINE));

        System.out.println("-- 4. logging an exception (message + stack trace)");
        try {
            Integer.parseInt("12x");
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "bad quantity in request", e);
        }

        System.out.println("-- 5. a custom handler with its own format");
        Logger audit = Logger.getLogger("ch18.audit");
        audit.setUseParentHandlers(false);                // don't ALSO send to the root's ConsoleHandler
        Handler h = new StreamHandler(System.out, new java.util.logging.Formatter() {
            @Override public String format(LogRecord r) {
                return "AUDIT|" + r.getLevel() + "|" + r.getLoggerName() + "|" + formatMessage(r) + "\n";
            }
        });
        audit.addHandler(h);
        audit.info("ann changed her e-mail");
        h.flush();

        System.out.println("-- 6. System.Logger (Java 9): a facade that routes to JUL by default");
        System.Logger sys = System.getLogger("ch18.logging.system");
        sys.log(System.Logger.Level.INFO, "hello from System.Logger, {0}", "ann");
        sys.log(System.Logger.Level.DEBUG, "DEBUG maps to FINE -> shown, ch18.logging is FINE");
    }
}
