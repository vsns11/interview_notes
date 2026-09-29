package lld.ch08.singleton;

import java.io.Serial;
import java.io.Serializable;

/** The same singleton with the three classic defences. */
public final class HardenedSingleton extends CloneableBase implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    public static final HardenedSingleton INSTANCE = new HardenedSingleton();

    private HardenedSingleton() {
        // Defence 1 - reflection: refuse a second construction.
        // (Works reliably with eager init, because INSTANCE is set during class init.)
        if (INSTANCE != null) {
            throw new IllegalStateException("Singleton already exists - use INSTANCE");
        }
    }

    // Defence 2 - serialization: replace whatever was deserialized with the real instance.
    @Serial
    private Object readResolve() { return INSTANCE; }

    // Defence 3 - cloning: override the inherited public clone() and refuse.
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Singleton cannot be cloned");
    }
}
