package lld.ch11.eventsourcing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Append-only log per stream (cart id). The log IS the source of truth. */
public final class EventStore {
    private final Map<String, List<CartEvent>> streams = new ConcurrentHashMap<>();

    public synchronized void append(String streamId, CartEvent e) {
        streams.computeIfAbsent(streamId, k -> new ArrayList<>()).add(e);
    }

    public synchronized List<CartEvent> read(String streamId) {
        return List.copyOf(streams.getOrDefault(streamId, List.of()));
    }
}
