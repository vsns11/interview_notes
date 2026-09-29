import java.util.*;
import java.util.concurrent.*;

/**
 * Configuration Externalization — runnable demo.
 *
 *   javac ConfigExternalizationDemo.java && java ConfigExternalizationDemo
 *
 * Shows:
 *   1. Hardcoded config: changing a value needs a rebuild
 *   2. Externalized config fetched at runtime
 *   3. Refresh WITHOUT restart - the whole point of the pattern
 *   4. Environment layering (default <- environment <- instance)
 *   5. Config server outage survived by client-side caching
 *   6. Feature flags flipped centrally, affecting every service at once
 */
public class ConfigExternalizationDemo {

    // ---------- 1. The "before": values frozen into the build ----------
    static class HardcodedService {
        // Changing either of these = edit source, recompile, redeploy
        private static final String DB_URL  = "jdbc:postgresql://prod-db-01:5432/orders";
        private static final int    TIMEOUT = 3000;

        String describe() { return "db=" + DB_URL + " timeout=" + TIMEOUT + "ms"; }
    }

    // ---------- 2. The config server ----------
    static class ConfigServer {
        // layer -> key -> value.  Later layers win.
        private final Map<String, Map<String, String>> layers = new LinkedHashMap<>();
        private int version = 1;
        volatile boolean up = true;

        ConfigServer() {
            layers.put("default",     new LinkedHashMap<>());
            layers.put("environment", new LinkedHashMap<>());
            layers.put("instance",    new LinkedHashMap<>());
        }

        void set(String layer, String key, String value) {
            layers.get(layer).put(key, value);
            version++;
            System.out.printf("    [config-server] %s.%s = %s   (v%d)%n", layer, key, value, version);
        }

        void clear(String layer, String key) {
            layers.get(layer).remove(key);
            version++;
            System.out.printf("    [config-server] removed %s.%s   (v%d)%n", layer, key, version);
        }

        /** Resolve with precedence: default <- environment <- instance. */
        Map<String, String> fetch() {
            if (!up) throw new IllegalStateException("config server unreachable");
            Map<String, String> merged = new LinkedHashMap<>();
            layers.values().forEach(merged::putAll);
            return merged;
        }

        int version() { return version; }
    }

    // ---------- 3. The client, with cache and refresh ----------
    static class ConfigClient {
        private final ConfigServer server;
        private volatile Map<String, String> cache = Map.of();
        private volatile int cachedVersion = 0;

        ConfigClient(ConfigServer server) {
            this.server = server;
            refresh();
        }

        /** Called at startup, on an interval, or via POST /actuator/refresh. */
        void refresh() {
            try {
                cache = server.fetch();
                cachedVersion = server.version();
                System.out.printf("    [client] refreshed config (v%d, %d keys)%n",
                        cachedVersion, cache.size());
            } catch (IllegalStateException e) {
                System.out.printf("    [client] server DOWN - keeping cached config (v%d)%n",
                        cachedVersion);
            }
        }

        String get(String key, String fallback) {
            return cache.getOrDefault(key, fallback);
        }

        boolean flagEnabled(String key) {
            return Boolean.parseBoolean(cache.getOrDefault(key, "false"));
        }
    }

    // ---------- 4. A service that reads config at USE time, not startup ----------
    static class OrderService {
        private final ConfigClient config;
        private final String name;

        OrderService(String name, ConfigClient config) { this.name = name; this.config = config; }

        String describe() {
            return String.format("%s: db=%s timeout=%sms retries=%s",
                    name,
                    config.get("db.url", "<unset>"),
                    config.get("http.timeoutMs", "1000"),
                    config.get("http.retries", "0"));
        }

        String checkout() {
            // Reading the flag on every call is what makes runtime flips work
            return config.flagEnabled("feature.newCheckout")
                    ? "NEW checkout flow"
                    : "legacy checkout flow";
        }
    }

    public static void main(String[] args) {

        System.out.println("\n=== 1. Hardcoded configuration ===");
        System.out.println("  " + new HardcodedService().describe());
        System.out.println("  >> to change the timeout: edit source, rebuild, redeploy.");
        System.out.println("     Across 50 services that is 50 deployments.");

        System.out.println("\n=== 2. Externalized: fetched at runtime ===");
        ConfigServer server = new ConfigServer();
        server.set("default",     "http.timeoutMs", "1000");
        server.set("default",     "http.retries",   "2");
        server.set("default",     "feature.newCheckout", "false");
        server.set("environment", "db.url", "jdbc:postgresql://prod-db-01:5432/orders");

        ConfigClient client = new ConfigClient(server);
        OrderService svc1 = new OrderService("order-svc-1", client);
        OrderService svc2 = new OrderService("order-svc-2", client);
        System.out.println("  " + svc1.describe());
        System.out.println("  " + svc2.describe());

        System.out.println("\n=== 3. Change a value + refresh, NO restart ===");
        server.set("environment", "http.timeoutMs", "5000");
        System.out.println("  before refresh: " + svc1.describe());
        client.refresh();
        System.out.println("  after refresh:  " + svc1.describe());
        System.out.println("  >> same running process, new value");

        System.out.println("\n=== 4. Layering: instance overrides environment overrides default ===");
        server.set("instance", "http.timeoutMs", "9000");
        client.refresh();
        System.out.println("  " + svc1.describe());
        System.out.println("  >> instance layer won (9000), overriding environment (5000)");

        System.out.println("\n=== 5. Feature flag flipped centrally ===");
        System.out.println("  checkout now: " + svc1.checkout());
        server.set("environment", "feature.newCheckout", "true");
        client.refresh();
        System.out.println("  checkout now: " + svc1.checkout());
        System.out.println("  and svc2 too: " + svc2.checkout());
        System.out.println("  >> every service flipped at once, with no deployment");

        System.out.println("\n=== 6. Config server outage ===");
        server.up = false;
        client.refresh();
        System.out.println("  " + svc1.describe());
        System.out.println("  checkout still: " + svc1.checkout());
        System.out.println("  >> running services keep working on cached config.");
        System.out.println("     Only NEW instances and NEW changes are blocked.");

        System.out.println("\n=== 7. Server returns; instance override removed ===");
        server.up = true;
        server.set("environment", "http.timeoutMs", "2500");
        client.refresh();
        System.out.println("  with instance override still set: " + svc1.describe());
        System.out.println("  >> still 9000 - the instance layer wins, as designed");

        server.clear("instance", "http.timeoutMs");
        client.refresh();
        System.out.println("  after removing it:               " + svc1.describe());
        System.out.println("  >> now the environment value (2500) applies");
    }
}
