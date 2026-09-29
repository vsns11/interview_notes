import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sidecar Pattern — runnable demo.
 *
 *   javac SidecarDemo.java && java SidecarDemo
 *
 * Shows:
 *   1. An application with cross-cutting concerns baked in (the "before")
 *   2. The SAME application with all of it removed, and a sidecar intercepting
 *   3. Adding mTLS + retry + metrics with ZERO application changes
 *   4. Why a sidecar beats a shared library in a polyglot estate
 *   5. The resource multiplier - the real cost of running one per instance
 */
public class SidecarDemo {

    // ================= BEFORE: concerns tangled into the app =================

    static class AppWithEverythingInline {
        private final AtomicInteger requestCount = new AtomicInteger();

        String handleRequest(String payload) {
            long start = System.nanoTime();
            requestCount.incrementAndGet();                       // metrics
            System.out.println("      [app] logging inbound request");   // logging
            if (!payload.startsWith("signed:")) {                 // security
                return "401";
            }
            String result = businessLogic(payload);               // <- the only real work
            System.out.printf("      [app] emitting latency metric: %.1fms%n",
                    (System.nanoTime() - start) / 1_000_000.0);
            return result;
        }

        private String businessLogic(String payload) {
            return "order-created:" + payload.substring(7);
        }
    }

    // ================= AFTER: the app does one thing =================

    /** Notice: no logging, no auth, no metrics, no retry. Business logic only. */
    static class CleanApp {
        String handleRequest(String payload) {
            return "order-created:" + payload;
        }
    }

    /** A service the app calls. Fails a couple of times to prove retry works. */
    static class UpstreamService {
        private final AtomicInteger calls = new AtomicInteger();
        private int failuresRemaining;

        UpstreamService(int failuresRemaining) { this.failuresRemaining = failuresRemaining; }

        String call(String req) {
            calls.incrementAndGet();
            if (failuresRemaining-- > 0) throw new RuntimeException("connection reset");
            return "inventory-ok";
        }

        int calls() { return calls.get(); }
    }

    // ================= THE SIDECAR: intercepts, app never knows =================

    static class Sidecar {
        private final CleanApp app;
        private final UpstreamService upstream;
        private final Map<String, Integer> metrics = new ConcurrentHashMap<>();
        private final String serviceName;

        Sidecar(String serviceName, CleanApp app, UpstreamService upstream) {
            this.serviceName = serviceName;
            this.app = app;
            this.upstream = upstream;
        }

        /** INBOUND: everything below happens before the app sees the request. */
        String handleInbound(String rawRequest, String clientCert) {
            metrics.merge("requests_total", 1, Integer::sum);
            long start = System.nanoTime();

            log("inbound intercepted");

            if (!"valid-cert".equals(clientCert)) {          // mTLS, in the sidecar
                metrics.merge("requests_rejected", 1, Integer::sum);
                log("mTLS verification FAILED - rejecting, app never invoked");
                return "403 Forbidden";
            }
            log("mTLS verified");

            String response = app.handleRequest(rawRequest);   // <- plain call, no plumbing

            double ms = (System.nanoTime() - start) / 1_000_000.0;
            metrics.merge("requests_ok", 1, Integer::sum);
            log(String.format("recorded latency %.2fms", ms));
            return response;
        }

        /** OUTBOUND: the app "calls upstream"; really it calls the sidecar. */
        String callUpstream(String request) {
            int attempts = 0;
            RuntimeException last = null;
            while (attempts < 3) {
                attempts++;
                try {
                    String r = upstream.call(request);
                    log("upstream ok on attempt " + attempts);
                    metrics.merge("upstream_ok", 1, Integer::sum);
                    return r;
                } catch (RuntimeException e) {
                    last = e;
                    metrics.merge("upstream_retries", 1, Integer::sum);
                    log("upstream failed (" + e.getMessage() + "), retrying");
                    sleep(50L * attempts);                   // backoff, in the sidecar
                }
            }
            throw last;
        }

        Map<String, Integer> metrics() { return new TreeMap<>(metrics); }

        private void log(String msg) {
            System.out.printf("    [sidecar:%s] %s%n", serviceName, msg);
        }
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) {

        System.out.println("\n=== 1. BEFORE: cross-cutting concerns inside the app ===");
        AppWithEverythingInline before = new AppWithEverythingInline();
        System.out.println("  -> " + before.handleRequest("signed:A-42"));
        System.out.println("  >> auth, logging and metrics code all live in the service.");
        System.out.println("     Every service in every language must reimplement it.");

        System.out.println("\n=== 2. AFTER: the app is only business logic ===");
        CleanApp clean = new CleanApp();
        UpstreamService inventory = new UpstreamService(2);   // fails twice, then works
        Sidecar sidecar = new Sidecar("order-service", clean, inventory);

        System.out.println("  -> " + sidecar.handleInbound("A-42", "valid-cert"));

        System.out.println("\n=== 3. mTLS enforced by the sidecar ===");
        System.out.println("  -> " + sidecar.handleInbound("A-43", "forged-cert"));
        System.out.println("  >> rejected before the application was ever invoked");

        System.out.println("\n=== 4. Retry + backoff, also in the sidecar ===");
        System.out.println("  -> " + sidecar.callUpstream("GET /stock/A-42"));
        System.out.printf("  upstream was actually called %d times; the app asked once%n",
                inventory.calls());
        System.out.println("  >> CleanApp contains no retry code whatsoever");

        System.out.println("\n=== 5. Telemetry collected without any app instrumentation ===");
        sidecar.metrics().forEach((k, v) -> System.out.printf("      %-18s %d%n", k, v));

        System.out.println("\n=== 6. The polyglot argument ===");
        System.out.println("  A shared library needs one implementation PER LANGUAGE:");
        for (String lang : List.of("Java", "Go", "Python", "Node", "Rust")) {
            System.out.printf("      %-7s -> its own retry/mTLS/metrics library to build and patch%n", lang);
        }
        System.out.println("  One sidecar binary serves all five - it speaks HTTP, not Java.");

        System.out.println("\n=== 7. The cost: resource multiplier ===");
        int[] podCounts = {10, 100, 1000, 5000};
        int sidecarMemMb = 100;
        for (int pods : podCounts) {
            System.out.printf("      %,5d pods x %dMB sidecar = %,6d MB (%.1f GB) of RAM%n",
                    pods, sidecarMemMb, pods * sidecarMemMb, pods * sidecarMemMb / 1024.0);
        }
        System.out.println("  >> this is the main objection to adopting a service mesh");
    }
}
