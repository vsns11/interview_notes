package lld.ch03.realization;

import java.util.ArrayList;
import java.util.List;

/** Realizes TWO interfaces. A class may implement many interfaces but extend only one class. */
public final class EmailNotifier implements Notifier, Auditable {
    private final List<String> sent = new ArrayList<>();

    @Override public void send(String to, String message) {
        String line = "EMAIL to " + to + ": " + message;
        sent.add(line);
        System.out.println("  " + line);
    }

    @Override public List<String> history() { return List.copyOf(sent); }
}
