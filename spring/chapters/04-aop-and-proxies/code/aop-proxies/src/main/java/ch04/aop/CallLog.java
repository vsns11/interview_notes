package ch04.aop;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** A tiny in-memory log so tests can see what the aspects did, in order. */
@Component
public class CallLog {
    private final List<String> lines = new ArrayList<>();

    public synchronized void add(String line) { lines.add(line); }
    public synchronized List<String> lines() { return List.copyOf(lines); }
    public synchronized void clear() { lines.clear(); }
}
