import java.util.*;
import java.util.concurrent.*;

/**
 * API Gateway Pattern — runnable demo.
 *
 *   javac ApiGatewayDemo.java && java ApiGatewayDemo
 *
 * Shows:
 *   1. Client-side calls (client must know every service) vs gateway
 *   2. Sequential vs PARALLEL scatter-gather aggregation
 *   3. Cross-cutting concerns applied once: auth, rate limit, logging
 *   4. Partial failure handled with a fallback
 */
public class ApiGatewayDemo {

    // ---------- Backend microservices (each with its own latency) ----------
    static class Service {
        final String name; final int latencyMs; boolean healthy = true;
        Service(String name, int latencyMs) { this.name = name; this.latencyMs = latencyMs; }

        String fetch(String userId) throws InterruptedException {
            Thread.sleep(latencyMs);
            if (!healthy) throw new RuntimeException(name + " unavailable");
            return name + "-data";
        }
    }

    static final Service feed          = new Service("feed", 120);
    static final Service notifications = new Service("notifications", 90);
    static final Service profile       = new Service("profile", 60);

    // ---------- The gateway ----------
    static class ApiGateway {
        private final ExecutorService pool = Executors.newFixedThreadPool(8);
        private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
        private static final int RATE_LIMIT = 5;

        /** Cross-cutting concerns, applied once for every route. */
        private void authenticate(String token) {
            if (token == null || !token.startsWith("valid-")) {
                throw new SecurityException("401 Unauthorized");
            }
        }

        private void rateLimit(String userId) {
            int n = requestCounts.merge(userId, 1, Integer::sum);
            if (n > RATE_LIMIT) throw new RuntimeException("429 Too Many Requests");
        }

        /** SEQUENTIAL aggregation - each call waits for the previous one. */
        Map<String, String> getHomeSequential(String token, String userId) throws Exception {
            authenticate(token);
            Map<String, String> result = new LinkedHashMap<>();
            result.put("feed",          feed.fetch(userId));
            result.put("notifications", notifications.fetch(userId));
            result.put("profile",       profile.fetch(userId));
            return result;
        }

        /** PARALLEL scatter-gather - this is what a gateway is really for. */
        Map<String, String> getHomeParallel(String token, String userId) throws Exception {
            authenticate(token);
            rateLimit(userId);

            Future<String> fFeed    = pool.submit(() -> feed.fetch(userId));
            Future<String> fNotifs  = pool.submit(() -> notifications.fetch(userId));
            Future<String> fProfile = pool.submit(() -> profile.fetch(userId));

            Map<String, String> result = new LinkedHashMap<>();
            result.put("feed",          get(fFeed,    "feed-unavailable"));
            result.put("notifications", get(fNotifs,  "no-notifications"));
            result.put("profile",       get(fProfile, "profile-unavailable"));
            return result;
        }

        /** Partial failure: one bad service must not fail the whole page. */
        private String get(Future<String> f, String fallback) {
            try {
                return f.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                return fallback;
            }
        }

        void shutdown() { pool.shutdownNow(); }
    }

    static long timed(Callable<?> action) throws Exception {
        long start = System.currentTimeMillis();
        action.call();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws Exception {
        ApiGateway gateway = new ApiGateway();

        System.out.println("\n=== 1. Without a gateway: client calls each service itself ===");
        long clientSide = timed(() -> {
            feed.fetch("u1"); notifications.fetch("u1"); profile.fetch("u1");
            return null;
        });
        System.out.printf("  3 round trips from the client: %dms%n", clientSide);
        System.out.println("  >> on mobile add ~150ms latency PER trip: ~" + (clientSide + 450) + "ms");
        System.out.println("  >> and the client must know all 3 service addresses");

        System.out.println("\n=== 2. Gateway, sequential aggregation ===");
        long seq = timed(() -> gateway.getHomeSequential("valid-token", "u1"));
        System.out.printf("  1 client round trip, 3 sequential server calls: %dms%n", seq);

        System.out.println("\n=== 3. Gateway, PARALLEL scatter-gather ===");
        long par = timed(() -> gateway.getHomeParallel("valid-token", "u2"));
        System.out.printf("  1 client round trip, 3 parallel server calls: %dms%n", par);
        System.out.printf("  >> ~the slowest service (%dms), not the sum%n", feed.latencyMs);

        System.out.println("\n=== 4. Auth enforced once, at the edge ===");
        try {
            gateway.getHomeParallel("bad-token", "u3");
        } catch (SecurityException e) {
            System.out.println("  rejected: " + e.getMessage() + "  (no service was touched)");
        }

        System.out.println("\n=== 5. Partial failure -> graceful degradation ===");
        notifications.healthy = false;
        System.out.println("  " + gateway.getHomeParallel("valid-token", "u4"));
        System.out.println("  >> feed and profile still served; only notifications degraded");
        notifications.healthy = true;

        System.out.println("\n=== 6. Rate limiting, also at the edge ===");
        try {
            for (int i = 0; i < 8; i++) gateway.getHomeParallel("valid-token", "u5");
        } catch (RuntimeException e) {
            System.out.println("  " + e.getMessage() + " after 5 requests");
        }

        gateway.shutdown();
    }
}
