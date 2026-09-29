package lld.ch02.interfaces;

/** An interface is a contract: WHAT can be done, not HOW. */
public interface Notifier {
    String send(String to, String message);        // abstract method: every implementer must provide it

    /** default method: implementers inherit it and may override it (added in Java 8). */
    default String sendAll(java.util.List<String> recipients, String message) {
        StringBuilder sb = new StringBuilder();
        for (String r : recipients) sb.append(send(r, clean(message))).append('\n');
        return sb.toString().stripTrailing();
    }

    /** static method: belongs to the interface itself; called as Notifier.console(). */
    static Notifier console() { return (to, msg) -> "[console] " + to + ": " + msg; }

    /** private method (Java 9+): a helper shared by default methods, hidden from implementers. */
    private String clean(String message) { return message.strip(); }
}
