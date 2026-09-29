package ch14.kafka;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** What each listener saw. The tests read it. */
@Component
public class Received {
    private final Map<String, List<String>> byListener = new ConcurrentHashMap<>();

    public void add(String listener, String line) {
        byListener.computeIfAbsent(listener, k -> new CopyOnWriteArrayList<>()).add(line);
    }

    public List<String> of(String listener) {
        return List.copyOf(byListener.getOrDefault(listener, List.of()));
    }
}
