package lld.ch02.interfaces;

public class EmailNotifier implements Notifier {
    @Override public String send(String to, String message) { return "[email] to " + to + ": " + message; }
}
