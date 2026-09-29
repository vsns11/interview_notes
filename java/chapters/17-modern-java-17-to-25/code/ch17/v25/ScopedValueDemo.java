package ch17.v25;

/** JEP 506 (Java 25): immutable, bounded-lifetime context passing -- a safer ThreadLocal. Details: Chapter 15. */
public class ScopedValueDemo {
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    static void handle() { audit("read order 17"); }                  // no user parameter threaded through
    static void audit(String action) {
        System.out.println("  audit: " + (USER.isBound() ? USER.get() : "<anonymous>") + " -> " + action);
    }

    public static void main(String[] args) {
        ScopedValue.where(USER, "ana").run(ScopedValueDemo::handle); // bound only while run() executes
        ScopedValue.where(USER, "bo").run(() -> {
            handle();
            ScopedValue.where(USER, "admin").run(ScopedValueDemo::handle);   // rebinding for a nested scope
            handle();                                                       // back to "bo"
        });
        handle();                                                            // unbound again
        System.out.println("bound outside? " + USER.isBound());
    }
}
