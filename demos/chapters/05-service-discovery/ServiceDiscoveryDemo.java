import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service Discovery Pattern — runnable demo.
 *
 *   javac ServiceDiscoveryDemo.java && java ServiceDiscoveryDemo
 *
 * Shows:
 *   1. Registration, heartbeats, and eviction of dead instances
 *   2. Client-side discovery with round-robin load balancing
 *   3. Server-side discovery (client knows only a fixed router)
 *   4. The stale-entry window, and why retry-another-instance is the answer
 *   5. Client-side caching surviving a registry outage
 */
public class ServiceDiscoveryDemo {

    // ---------- What the registry stores ----------
    record Instance(String serviceName, String host, int port, String version) {
        String address() { return host + ":" + port; }
    }

    static class Registration {
        final Instance instance;
        volatile long lastHeartbeat;
        Registration(Instance i) { this.instance = i; this.lastHeartbeat = System.currentTimeMillis(); }
    }

    // ---------- The registry ----------
    static class ServiceRegistry {
        // NOTE: List of instances per name - a Map<String,String> could never load balance
        private final Map<String, Map<String, Registration>> services = new ConcurrentHashMap<>();
        private final long ttlMs;
        volatile boolean up = true;

        ServiceRegistry(long ttlMs) { this.ttlMs = ttlMs; }

        void register(Instance i) {
            requireUp();
            services.computeIfAbsent(i.serviceName(), k -> new ConcurrentHashMap<>())
                    .put(i.address(), new Registration(i));
            System.out.printf("    [registry] registered %s at %s (v%s)%n",
                    i.serviceName(), i.address(), i.version());
        }

        void deregister(Instance i) {
            requireUp();
            Map<String, Registration> m = services.get(i.serviceName());
            if (m != null && m.remove(i.address()) != null) {
                System.out.printf("    [registry] DEREGISTERED %s%n", i.address());
            }
        }

        void heartbeat(Instance i) {
            if (!up) return;
            Map<String, Registration> m = services.get(i.serviceName());
            if (m != null) {
                Registration r = m.get(i.address());
                if (r != null) r.lastHeartbeat = System.currentTimeMillis();
            }
        }

        /** Health check: evict anything that stopped heartbeating. */
        void evictExpired() {
            long now = System.currentTimeMillis();
            services.forEach((name, m) ->
                m.entrySet().removeIf(e -> {
                    boolean dead = now - e.getValue().lastHeartbeat > ttlMs;
                    if (dead) System.out.printf("    [registry] EVICTED %s (no heartbeat)%n", e.getKey());
                    return dead;
                }));
        }

        List<Instance> discover(String serviceName) {
            requireUp();
            return services.getOrDefault(serviceName, Map.of())
                    .values().stream().map(r -> r.instance).collect(Collectors.toList());
        }

        private void requireUp() {
            if (!up) throw new IllegalStateException("registry unreachable");
        }
    }

    // ---------- Client-side discovery: smart client ----------
    static class DiscoveryClient {
        private final ServiceRegistry registry;
        private final Map<String, List<Instance>> cache = new ConcurrentHashMap<>();
        private final AtomicInteger counter = new AtomicInteger();

        DiscoveryClient(ServiceRegistry registry) { this.registry = registry; }

        /** Refresh cache if we can; otherwise keep serving last-known-good. */
        List<Instance> instancesOf(String serviceName) {
            try {
                List<Instance> fresh = registry.discover(serviceName);
                cache.put(serviceName, fresh);
                return fresh;
            } catch (IllegalStateException e) {
                List<Instance> stale = cache.getOrDefault(serviceName, List.of());
                System.out.printf("    [client] registry DOWN - using cached list (%d instances)%n",
                        stale.size());
                return stale;
            }
        }

        /** Round-robin across whatever instances we know about. */
        Instance choose(String serviceName) {
            List<Instance> list = instancesOf(serviceName);
            if (list.isEmpty()) throw new RuntimeException("no instances of " + serviceName);
            return list.get(Math.floorMod(counter.getAndIncrement(), list.size()));
        }
    }

    // ---------- Server-side discovery: dumb client, smart router ----------
    static class LoadBalancer {
        private final ServiceRegistry registry;
        private final AtomicInteger counter = new AtomicInteger();

        LoadBalancer(ServiceRegistry registry) { this.registry = registry; }

        /** The client calls THIS with a name and never sees the registry. */
        String forward(String serviceName, String request) {
            List<Instance> list = registry.discover(serviceName);
            if (list.isEmpty()) return "503 no instances";
            Instance target = list.get(Math.floorMod(counter.getAndIncrement(), list.size()));
            return "routed '" + request + "' -> " + target.address();
        }
    }

    public static void main(String[] args) throws Exception {
        ServiceRegistry registry = new ServiceRegistry(600);   // 600ms TTL for a fast demo
        DiscoveryClient client   = new DiscoveryClient(registry);
        LoadBalancer lb          = new LoadBalancer(registry);

        System.out.println("\n=== 1. Three instances register ===");
        Instance a = new Instance("order-service", "10.0.0.1", 8080, "1.0");
        Instance b = new Instance("order-service", "10.0.0.2", 8080, "1.0");
        Instance c = new Instance("order-service", "10.0.0.3", 8080, "2.0");
        registry.register(a); registry.register(b); registry.register(c);

        System.out.println("\n=== 2. Client-side discovery + round robin ===");
        for (int i = 0; i < 6; i++) {
            System.out.println("  call " + (i + 1) + " -> " + client.choose("order-service").address());
        }
        System.out.println("  >> load spread evenly; no address was ever hardcoded");

        System.out.println("\n=== 3. Server-side discovery: client knows only the LB ===");
        for (int i = 0; i < 3; i++) {
            System.out.println("  " + lb.forward("order-service", "POST /orders"));
        }

        System.out.println("\n=== 4. An instance dies (stops heartbeating) ===");
        // a and b keep heartbeating; c crashes silently
        for (int t = 0; t < 4; t++) {
            registry.heartbeat(a); registry.heartbeat(b);
            Thread.sleep(200);
        }
        registry.evictExpired();
        System.out.println("  live instances now: " +
                client.instancesOf("order-service").stream()
                      .map(Instance::address).toList());
        System.out.println("  >> crashed instance removed WITHOUT graceful deregistration");

        System.out.println("\n=== 5. The stale-entry window ===");
        Instance d = new Instance("order-service", "10.0.0.4", 8080, "1.0");
        registry.register(d);
        System.out.println("  d registered; it crashes immediately, before the next health check");
        System.out.println("  a client discovering NOW still gets: " +
                client.instancesOf("order-service").stream().map(Instance::address).toList());
        System.out.println("  >> unavoidable. The fix is not prevention but RETRY ON ANOTHER INSTANCE");

        System.out.println("\n=== 6. Registry outage - cache keeps the system alive ===");
        registry.up = false;
        for (int i = 0; i < 2; i++) {
            System.out.println("  still routing to " + client.choose("order-service").address());
        }
        System.out.println("  >> running services survive; only NEW registrations are blocked");

        System.out.println("\n=== 7. Filtering by metadata (canary routing) ===");
        registry.up = true;
        registry.register(new Instance("order-service", "10.0.0.5", 8080, "2.0"));
        List<Instance> all = client.instancesOf("order-service");
        System.out.println("  all instances: " + all.stream().map(Instance::address).toList());
        System.out.println("  v2.0 only:     " + all.stream()
                .filter(i -> i.version().equals("2.0"))
                .map(Instance::address).toList());
        System.out.println("  >> metadata lets you route canary traffic to a specific version");
    }
}
