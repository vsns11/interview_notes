package ch15.jmm;

import java.util.concurrent.CountDownLatch;

/** Correct ways to publish objects and lazily initialise singletons. */
public class SafePublication {

    // 1) Immutable object with final fields: safe to share even through a data race (if 'this' doesn't escape)
    record Config(String url, int poolSize) {}

    // 2) Double-checked locking: correct ONLY with volatile (since Java 5)
    static class Registry {
        private static volatile Registry instance;
        static int created = 0;
        private Registry() { created++; }
        static Registry get() {
            Registry r = instance;                      // one volatile read on the fast path
            if (r == null) {
                synchronized (Registry.class) {
                    r = instance;
                    if (r == null) instance = r = new Registry();
                }
            }
            return r;
        }
    }

    // 3) Initialization-on-demand holder: lazy, thread-safe, no locking code (the JVM's class-init lock does the work)
    static class Heavy {
        static int created = 0;
        private Heavy() { created++; }
        private static class Holder { static final Heavy INSTANCE = new Heavy(); }
        static Heavy get() { return Holder.INSTANCE; }
    }

    // 4) Publishing through a volatile field: everything written before the volatile write is visible after the read
    static volatile Config current;

    public static void main(String[] args) throws Exception {
        int n = 64;
        CountDownLatch go = new CountDownLatch(1);
        Thread[] ts = new Thread[n];
        for (int i = 0; i < n; i++) {
            ts[i] = new Thread(() -> {
                try { go.await(); } catch (InterruptedException e) { return; }
                Registry.get();
                Heavy.get();
            });
            ts[i].start();
        }
        go.countDown();                                  // release all 64 at once
        for (Thread t : ts) t.join();
        System.out.println("64 threads raced: Registry constructed " + Registry.created + " time(s), Heavy constructed " + Heavy.created + " time(s)");

        Thread reader = new Thread(() -> {
            Config c;
            while ((c = current) == null) Thread.onSpinWait();    // volatile read
            System.out.println("reader sees fully built config: " + c);
        });
        reader.start();
        current = new Config("jdbc:postgresql://db/shop", 10);    // volatile write publishes it safely
        reader.join();
    }
}
