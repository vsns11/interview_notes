package lld.ch03.realization;

/** An interface: a contract of operations with no state. Drawn as <<interface>>. */
public interface Notifier {
    void send(String to, String message);

    /** Java 8+ default method: implementers inherit this unless they override it. */
    default String channel() {
        String n = getClass().getSimpleName();
        return n.contains("$$Lambda") ? "lambda" : n;   // lambdas get generated class names
    }
}
