package lld.ch08.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 1. Eager: the instance is created when the class is initialised. Thread-safe for free. */
public final class EagerConfigStore {

    private static final EagerConfigStore INSTANCE = new EagerConfigStore();

    private final Map<String, String> values = new ConcurrentHashMap<>();

    private EagerConfigStore() {                       // private: nobody else can call new
        System.out.println("  [EagerConfigStore] reading settings from disk (runs once)");
        values.put("region", "us-east-1");
        values.put("retries", "3");
    }

    public static EagerConfigStore getInstance() { return INSTANCE; }

    public String get(String key)             { return values.get(key); }
    public void set(String key, String value) { values.put(key, value); }
}
