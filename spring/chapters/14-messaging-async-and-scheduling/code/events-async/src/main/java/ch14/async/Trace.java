package ch14.async;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** Thread-safe list of "what happened, on which thread" lines that the tests print and assert. */
@Component
public class Trace {
    private final List<String> lines = new CopyOnWriteArrayList<>();

    public void add(String line) { lines.add(line); }
    public List<String> lines() { return List.copyOf(lines); }
    public void clear() { lines.clear(); }

    /** Short thread description: name plus "(virtual)" for virtual threads. */
    public static String thread() {
        Thread t = Thread.currentThread();
        return t.isVirtual() ? "virtual " + (t.getName().isEmpty() ? "#" + t.threadId() : t.getName()) : t.getName();
    }
}
