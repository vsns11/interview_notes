package lld.ch20.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) throws Exception {
        for (PolicyType type : PolicyType.values()) {
            System.out.println("== " + type + " (capacity 3): put a,b,c; get b,b,b,c,a,a; put d");
            Cache<String, Integer> c = Cache.<String, Integer>builder().capacity(3).policy(type).build();
            c.addListener((k, v, why) -> System.out.println("  listener: " + k + "=" + v + " left, " + why));
            c.put("a", 1);
            c.put("b", 2);
            c.put("c", 3);
            for (String k : List.of("b", "b", "b", "c", "a", "a")) c.get(k);
            c.put("d", 4);
            List<String> present = new ArrayList<>();
            for (String k : List.of("a", "b", "c", "d")) if (c.get(k).isPresent()) present.add(k);
            System.out.println("  still cached: " + present);
        }

        System.out.println("== TTL with a manual clock");
        AtomicLong now = new AtomicLong(0);
        Cache<String, String> sessions = Cache.<String, String>builder().capacity(10).clock(now::get).build();
        sessions.addListener((k, v, why) -> System.out.println("  listener: " + k + " left, " + why));
        sessions.put("session:42", "alice", Duration.ofSeconds(5));
        sessions.put("session:43", "bob", Duration.ofSeconds(5));
        sessions.put("config", "v1");
        System.out.println("  t=0s  get session:42 -> " + sessions.get("session:42").orElse("MISS"));
        now.set(6_000);
        System.out.println("  t=6s  get session:42 -> " + sessions.get("session:42").orElse("MISS"));
        System.out.println("  sweeper purged " + sessions.purgeExpired() + " more expired entry");
        System.out.println("  config (no TTL) -> " + sessions.get("config").orElse("MISS"));
        sessions.invalidate("config");
        System.out.println("  stats: " + sessions.stats());

        System.out.println("== Sharded cache under 8 threads");
        ShardedCache<Integer, Integer> sharded = new ShardedCache<>(8, 1_000, PolicyType.LRU);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int t = 0; t < 8; t++) {
            int seed = t;
            pool.submit(() -> {
                for (int i = 0; i < 50_000; i++) {
                    int key = (i * 31 + seed) % 20_000;
                    if (sharded.get(key).isEmpty()) sharded.put(key, key * 2);
                }
            });
        }
        pool.shutdown();
        System.out.println("  finished: " + pool.awaitTermination(30, TimeUnit.SECONDS)
                + ", size <= 8000: " + (sharded.size() <= 8_000));
    }
}
