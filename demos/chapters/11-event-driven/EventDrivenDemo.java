import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Event-Driven Architecture — runnable demo.
 *
 *   javac EventDrivenDemo.java && java EventDrivenDemo
 *
 * Shows:
 *   1. One event fanning out to many independent consumers
 *   2. Adding a NEW consumer without touching the producer
 *   3. Async delivery: the producer does not wait for consumers
 *   4. One consumer failing does not affect the others
 *   5. At-least-once delivery -> duplicates -> why consumers must be idempotent
 *   6. Correlation IDs, because debugging across services is the real cost
 */
public class EventDrivenDemo {

    // ---------- Events are immutable facts about the past ----------
    record Event(String type, String correlationId, Map<String, String> payload) {
        Event {
            payload = Map.copyOf(payload);       // defensive copy: nobody mutates history
        }
    }

    // ---------- The bus ----------
    static class EventBus {
        private final Map<String, List<Subscriber>> subscribers = new ConcurrentHashMap<>();
        private final ExecutorService pool = Executors.newFixedThreadPool(4);
        private final boolean async;

        record Subscriber(String name, Consumer<Event> handler) {}

        EventBus(boolean async) { this.async = async; }

        void subscribe(String eventType, String name, Consumer<Event> handler) {
            subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                       .add(new Subscriber(name, handler));
            System.out.printf("    [bus] '%s' subscribed to %s%n", name, eventType);
        }

        void publish(Event e) {
            List<Subscriber> subs = subscribers.getOrDefault(e.type(), List.of());
            System.out.printf("    [bus] publishing %s (cid=%s) to %d subscriber(s)%n",
                    e.type(), e.correlationId(), subs.size());

            for (Subscriber s : subs) {
                Runnable task = () -> {
                    try {
                        s.handler().accept(e);
                    } catch (Exception ex) {
                        // One consumer failing must NOT stop the others
                        System.out.printf("      [%s] FAILED: %s (isolated)%n", s.name(), ex.getMessage());
                    }
                };
                if (async) pool.submit(task); else task.run();
            }
        }

        void shutdown() throws InterruptedException {
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
        }
    }

    // ---------- Consumers ----------
    static void notifyFollowers(Event e) {
        sleep(60);
        System.out.printf("      [followers]  notified for photo %s (cid=%s)%n",
                e.payload().get("photoId"), e.correlationId());
    }

    static void updateProfile(Event e) {
        sleep(40);
        System.out.printf("      [profile]    photo count incremented (cid=%s)%n", e.correlationId());
    }

    static void moderate(Event e) {
        sleep(80);
        throw new RuntimeException("moderation service unavailable");
    }

    /** Added later, with zero changes to the producer. */
    static void generateThumbnail(Event e) {
        sleep(50);
        System.out.printf("      [thumbnail]  generated for %s (cid=%s)%n",
                e.payload().get("photoId"), e.correlationId());
    }

    // ---------- Idempotent consumer: survives duplicate delivery ----------
    static class BillingConsumer {
        private final Set<String> processed = ConcurrentHashMap.newKeySet();
        private int chargesApplied = 0;

        void handle(Event e) {
            String eventId = e.payload().get("eventId");
            if (!processed.add(eventId)) {              // add() returns false if already present
                System.out.printf("      [billing]    duplicate %s ignored%n", eventId);
                return;
            }
            chargesApplied++;
            System.out.printf("      [billing]    charge applied (total=%d)%n", chargesApplied);
        }

        int chargesApplied() { return chargesApplied; }
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("\n=== 1. SYNCHRONOUS bus: producer waits for every consumer ===");
        EventBus syncBus = new EventBus(false);
        syncBus.subscribe("PhotoUploaded", "followers", EventDrivenDemo::notifyFollowers);
        syncBus.subscribe("PhotoUploaded", "profile",   EventDrivenDemo::updateProfile);

        long t0 = System.currentTimeMillis();
        syncBus.publish(new Event("PhotoUploaded", "cid-1", Map.of("photoId", "p-100")));
        System.out.printf("  producer blocked for %dms  <- the sum of all consumers%n",
                System.currentTimeMillis() - t0);

        System.out.println("\n=== 2. ASYNCHRONOUS bus: producer returns immediately ===");
        EventBus bus = new EventBus(true);
        bus.subscribe("PhotoUploaded", "followers", EventDrivenDemo::notifyFollowers);
        bus.subscribe("PhotoUploaded", "profile",   EventDrivenDemo::updateProfile);
        bus.subscribe("PhotoUploaded", "moderation", EventDrivenDemo::moderate);

        long t1 = System.currentTimeMillis();
        bus.publish(new Event("PhotoUploaded", "cid-2", Map.of("photoId", "p-200")));
        System.out.printf("  producer returned in %dms  <- did NOT wait%n",
                System.currentTimeMillis() - t1);
        Thread.sleep(300);

        System.out.println("\n=== 3. One consumer failed - did the others care? ===");
        System.out.println("  >> moderation threw, followers and profile still completed above");

        System.out.println("\n=== 4. Add a NEW consumer - producer code untouched ===");
        bus.subscribe("PhotoUploaded", "thumbnail", EventDrivenDemo::generateThumbnail);
        bus.publish(new Event("PhotoUploaded", "cid-3", Map.of("photoId", "p-300")));
        Thread.sleep(300);
        System.out.println("  >> 4 reactions now. The upload service never changed.");

        System.out.println("\n=== 5. At-least-once delivery -> idempotent consumers ===");
        BillingConsumer billing = new BillingConsumer();
        EventBus billingBus = new EventBus(false);
        billingBus.subscribe("OrderPlaced", "billing", billing::handle);

        Event order = new Event("OrderPlaced", "cid-4", Map.of("eventId", "evt-abc"));
        billingBus.publish(order);
        billingBus.publish(order);      // redelivery after a failed ack
        billingBus.publish(order);
        System.out.println("  charges actually applied: " + billing.chargesApplied() + " (must be 1)");

        System.out.println("\n=== 6. Correlation IDs make async debugging possible ===");
        System.out.println("  >> every log line above carries cid=... - that is how you");
        System.out.println("     reconstruct one causal chain across services and time");

        bus.shutdown();
    }
}
