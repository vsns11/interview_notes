package lld.ch08.singleton;

import java.util.concurrent.atomic.AtomicInteger;

/** 2. Naive lazy: NOT thread-safe. Two threads can both see null and both construct. */
final class NaiveLazySingleton {
    static final AtomicInteger CONSTRUCTED = new AtomicInteger();
    private static NaiveLazySingleton instance;

    private NaiveLazySingleton() {
        CONSTRUCTED.incrementAndGet();
        try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static NaiveLazySingleton getInstance() {
        if (instance == null) {                 // check ...
            instance = new NaiveLazySingleton(); // ... then act: a race window
        }
        return instance;
    }
}

/** 3. Synchronized lazy: correct, but every call pays for the lock. */
final class SynchronizedLazySingleton {
    static final AtomicInteger CONSTRUCTED = new AtomicInteger();
    private static SynchronizedLazySingleton instance;

    private SynchronizedLazySingleton() {
        CONSTRUCTED.incrementAndGet();
        try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static synchronized SynchronizedLazySingleton getInstance() {
        if (instance == null) instance = new SynchronizedLazySingleton();
        return instance;
    }
}

/** 4. Double-checked locking: lock only on the first calls. volatile is mandatory. */
final class DclSingleton {
    static final AtomicInteger CONSTRUCTED = new AtomicInteger();
    private static volatile DclSingleton instance;

    private DclSingleton() {
        CONSTRUCTED.incrementAndGet();
        try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static DclSingleton getInstance() {
        DclSingleton local = instance;           // one volatile read on the fast path
        if (local == null) {                     // 1st check, no lock
            synchronized (DclSingleton.class) {
                local = instance;
                if (local == null) {             // 2nd check, under the lock
                    local = new DclSingleton();
                    instance = local;            // volatile write publishes a fully built object
                }
            }
        }
        return local;
    }
}

/** 5. Bill Pugh / initialization-on-demand holder: lazy, lock-free, thread-safe via class loading. */
final class HolderSingleton {
    static final AtomicInteger CONSTRUCTED = new AtomicInteger();

    private HolderSingleton() {
        CONSTRUCTED.incrementAndGet();
        System.out.println("  [HolderSingleton] constructed on first getInstance()");
    }

    private static final class Holder {          // not initialised until first touched
        private static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    static HolderSingleton getInstance() { return Holder.INSTANCE; }
}
