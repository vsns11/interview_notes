package lld.ch06.boundaries;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lld.ch06.testing.MiniTest;

/** Boundaries: wrap third-party / general-purpose APIs, learn them with tests, and define interfaces for code that doesn't exist yet. */
public class Main {

    record Session(String userId, Instant expiresAt) {}

    // ---------------- BEFORE: a raw Map passed all over the codebase ----------------
    static class Before {
        // Every caller can clear(), put(null, ...), put wrong types... and every caller must know the key format.
        static void loginHandler(Map<String, Session> sessions, String token, String user) {
            sessions.put(token, new Session(user, Instant.parse("2026-09-28T12:00:00Z")));
        }
        static void buggyAdminTool(Map<String, Session> sessions) { sessions.clear(); }   // oops: everyone logged out
    }

    // ---------------- AFTER: the Map is hidden behind an interface shaped for OUR needs ----------------
    static class After {
        static final class SessionRegistry {
            private final Map<String, Session> byToken = new ConcurrentHashMap<>();   // boundary type stays private

            void open(String token, Session session) { byToken.put(token, session); }
            Optional<Session> activeSession(String token, Instant now) {
                return Optional.ofNullable(byToken.get(token)).filter(s -> now.isBefore(s.expiresAt()));
            }
            void close(String token) { byToken.remove(token); }
            int size() { return byToken.size(); }
        }

        /** The fraud team's API isn't written yet: we define the interface WE want and code against it. */
        interface FraudCheck { boolean isSuspicious(String userId, long amountPaise); }

        /** Fake used until the real client exists; later an Adapter will wrap the real API behind the same interface. */
        static final class FakeFraudCheck implements FraudCheck {
            public boolean isSuspicious(String userId, long amountPaise) { return amountPaise > 10_000_000; }
        }
    }

    /** LEARNING TESTS: executable notes about how third-party (here: JDK) APIs really behave. Re-run them on every upgrade. */
    public static class JdkLearningTest {
        @lld.ch06.testing.Test void hashmap_allows_one_null_key() {
            Map<String, Integer> m = new HashMap<>();
            m.put(null, 1);
            MiniTest.assertEquals(1, m.get(null));
        }
        @lld.ch06.testing.Test void concurrenthashmap_rejects_null_keys() {
            MiniTest.assertThrows(NullPointerException.class, () -> new ConcurrentHashMap<String, Integer>().put(null, 1));
        }
        @lld.ch06.testing.Test void map_of_rejects_duplicate_keys() {
            MiniTest.assertThrows(IllegalArgumentException.class, () -> Map.of("k", 1, "k", 2));
        }
        @lld.ch06.testing.Test void string_split_drops_trailing_empty_strings() {
            MiniTest.assertEquals(2, "a,b,,".split(",").length);
            MiniTest.assertEquals(4, "a,b,,".split(",", -1).length);
        }
    }

    public static void main(String[] args) {
        System.out.println("== BEFORE ==");
        Map<String, Session> raw = new HashMap<>();
        Before.loginHandler(raw, "t1", "asha");
        Before.buggyAdminTool(raw);
        System.out.println("sessions after admin tool: " + raw.size() + "  <-- nothing stopped the clear()");

        System.out.println("== AFTER ==");
        var registry = new After.SessionRegistry();
        registry.open("t1", new Session("asha", Instant.parse("2026-09-28T12:00:00Z")));
        System.out.println("active at 11:00? " + registry.activeSession("t1", Instant.parse("2026-09-28T11:00:00Z")).isPresent());
        System.out.println("active at 13:00? " + registry.activeSession("t1", Instant.parse("2026-09-28T13:00:00Z")).isPresent());
        After.FraudCheck fraud = new After.FakeFraudCheck();
        System.out.println("fraud(asha, Rs 2,00,000)? " + fraud.isSuspicious("asha", 20_000_000));

        System.out.println("== Learning tests ==");
        var summary = MiniTest.run(JdkLearningTest.class);
        System.out.println(summary.passed() + " passed, " + summary.failed() + " failed");
    }
}
