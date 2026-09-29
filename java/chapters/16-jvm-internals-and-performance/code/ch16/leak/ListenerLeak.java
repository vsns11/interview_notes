package ch16.leak;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A classic leak: every request subscribes a listener to a long-lived event bus and never unsubscribes.
 * The listener captures the request's 10 KiB buffer, so every buffer stays reachable from a static field.
 * Usage: java -cp out ch16.leak.ListenerLeak [leak|fixed|both] [holdSeconds]
 */
public class ListenerLeak {

    /** Long-lived, application-wide object: lives as long as the class (a GC root via a static field). */
    static final class EventBus {
        static final List<Consumer<String>> LISTENERS = new CopyOnWriteArrayList<>();

        static AutoCloseable subscribe(Consumer<String> l) {
            LISTENERS.add(l);
            return () -> LISTENERS.remove(l);                 // the handle that lets callers clean up
        }
        static void publish(String event) { LISTENERS.forEach(l -> l.accept(event)); }
    }

    static final class RequestSession {
        final byte[] buffer = new byte[10 * 1024];            // per-request working memory
        void onEvent(String e) { buffer[0]++; }
    }

    static void handleRequestLeaky() {
        RequestSession s = new RequestSession();
        EventBus.subscribe(s::onEvent);                       // BUG: never unsubscribed
        // ... handle the request ...
    }

    static void handleRequestFixed() throws Exception {
        RequestSession s = new RequestSession();
        try (AutoCloseable sub = EventBus.subscribe(s::onEvent)) {   // unsubscribed when the request ends
            // ... handle the request ...
        }
    }

    static long usedAfterGcKiB() {
        System.gc();                                          // only for measuring in a demo, never in production code
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1024;
    }

    static void run(String mode) throws Exception {
        EventBus.LISTENERS.clear();
        long base = usedAfterGcKiB();
        System.out.println("mode = " + mode);
        for (int round = 1; round <= 5; round++) {
            for (int i = 0; i < 2_000; i++) {
                if (mode.equals("leak")) handleRequestLeaky(); else handleRequestFixed();
            }
            EventBus.publish("tick");
            System.out.printf("  after %,6d requests: listeners=%,6d  heap growth since start=%,7d KiB%n",
                    round * 2_000, EventBus.LISTENERS.size(), usedAfterGcKiB() - base);
        }
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "both";
        if (mode.equals("both")) { run("leak"); run("fixed"); } else run(mode);
        if (args.length > 1) {                                // keep the JVM alive so jcmd/jstat can attach
            System.out.println("holding for " + args[1] + " s, pid " + ProcessHandle.current().pid());
            Thread.sleep(Long.parseLong(args[1]) * 1000);
        }
    }
}
