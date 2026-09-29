package lld.ch08.prototype;

import java.util.HashMap;
import java.util.Map;

/** Prototype registry (a.k.a. prototype manager): named, pre-configured objects handed out as copies. */
final class ShapeRegistry {
    private final Map<String, Shape> prototypes = new HashMap<>();

    void register(String key, Shape prototype) { prototypes.put(key, prototype); }

    Shape create(String key) {
        Shape p = prototypes.get(key);
        if (p == null) throw new IllegalArgumentException("no prototype named " + key);
        return p.copy();                                 // never hand out the original
    }
}
