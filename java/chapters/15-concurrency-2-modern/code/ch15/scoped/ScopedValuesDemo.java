package ch15.scoped;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Passing context implicitly: ThreadLocal (Java 1.2) vs ScopedValue (final in Java 25, JEP 506). */
public class ScopedValuesDemo {

    // ---------- ThreadLocal: a per-thread mutable variable ----------
    static final ThreadLocal<String> REQUEST_ID_TL = new ThreadLocal<>();
    static final ThreadLocal<StringBuilder> BUFFER = ThreadLocal.withInitial(StringBuilder::new);

    // ---------- ScopedValue: an immutable value bound for the duration of a call ----------
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        // 1) ThreadLocal: each thread sees its own value
        Thread a = new Thread(() -> { REQUEST_ID_TL.set("req-A"); log("ThreadLocal in A"); REQUEST_ID_TL.remove(); });
        Thread b = new Thread(() -> { REQUEST_ID_TL.set("req-B"); log("ThreadLocal in B"); REQUEST_ID_TL.remove(); });
        a.start(); a.join(); b.start(); b.join();
        System.out.println("1 main thread sees " + REQUEST_ID_TL.get() + " (never set here)");

        // 2) ThreadLocal pitfall: pooled threads are reused, so a value that isn't removed LEAKS into the next task
        try (ExecutorService single = Executors.newSingleThreadExecutor()) {
            single.submit(() -> REQUEST_ID_TL.set("req-OLD")).get();          // task forgets remove()
            single.submit(() -> System.out.println("2 next task on the same pooled thread sees: " + REQUEST_ID_TL.get())).get();
        }
        BUFFER.get().append("per-thread scratch buffer");                    // withInitial: lazily created per thread
        System.out.println("2 withInitial -> " + BUFFER.get());
        BUFFER.remove();

        // 3) ScopedValue: bind for exactly one call; callees read it; it disappears afterwards
        System.out.println("3 bound before? " + USER.isBound());
        ScopedValue.where(USER, "alice").run(() -> handleRequest());
        System.out.println("3 bound after?  " + USER.isBound());

        // 4) call() returns a value; rebinding in a nested scope shadows, then restores
        String result = ScopedValue.where(USER, "bob").call(() -> {
            String outer = USER.get();
            String inner = ScopedValue.where(USER, "admin").call(USER::get);   // nested rebinding
            return outer + " -> " + inner + " -> " + USER.get();
        });
        System.out.println("4 nested rebinding: " + result);

        // 5) Reading an unbound value
        try {
            USER.get();
        } catch (NoSuchElementException e) {
            System.out.println("5 get() when unbound -> NoSuchElementException; orElse -> " + USER.orElse("anonymous"));
        }

        // 6) Not inherited by threads you start yourself (only by StructuredTaskScope forks)
        ScopedValue.where(USER, "carol").run(() -> {
            Thread t = Thread.ofVirtual().unstarted(() ->
                    System.out.println("6 plain new thread sees bound? " + USER.isBound()));
            t.start();
            try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
    }

    static void handleRequest() { service(); }                      // no parameter passing through layers
    static void service() { repository(); }
    static void repository() { System.out.println("3 repository sees USER = " + USER.get()); }

    static void log(String msg) { System.out.println("1 " + msg + ": requestId = " + REQUEST_ID_TL.get()); }
}
