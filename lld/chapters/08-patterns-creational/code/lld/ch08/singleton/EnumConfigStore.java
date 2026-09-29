package lld.ch08.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 6. Enum singleton: shortest form, safe against reflection and serialization by the JVM itself. */
public enum EnumConfigStore {
    INSTANCE;

    private final Map<String, String> values = new ConcurrentHashMap<>(Map.of("region", "us-east-1"));

    public String get(String key)             { return values.get(key); }
    public void set(String key, String value) { values.put(key, value); }
}
