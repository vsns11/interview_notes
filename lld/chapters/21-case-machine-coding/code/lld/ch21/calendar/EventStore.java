package lld.ch21.calendar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/** Repository: events by id and an index user -> date -> events. Not thread-safe on its own: CalendarService guards it. */
public class EventStore {
    private final Map<String, Event> byId = new ConcurrentHashMap<>();
    private final Map<String, TreeMap<LocalDate, List<Event>>> byUser = new ConcurrentHashMap<>();

    public void save(Event e) {
        byId.put(e.id(), e);
        for (String u : e.attendees())
            byUser.computeIfAbsent(u, k -> new TreeMap<>()).computeIfAbsent(e.date(), d -> new ArrayList<>()).add(e);
    }

    public void remove(Event e) {
        byId.remove(e.id());
        for (String u : e.attendees()) {
            List<Event> day = byUser.getOrDefault(u, new TreeMap<>()).get(e.date());
            if (day != null) day.removeIf(x -> x.id().equals(e.id()));
        }
    }

    public Optional<Event> find(String id) { return Optional.ofNullable(byId.get(id)); }

    public List<Event> onDay(String user, LocalDate date) {
        return List.copyOf(byUser.getOrDefault(user, new TreeMap<>()).getOrDefault(date, List.of()));
    }

    public List<Event> between(String user, LocalDate from, LocalDate to) {
        List<Event> out = new ArrayList<>();
        byUser.getOrDefault(user, new TreeMap<>()).subMap(from, true, to, true).values().forEach(out::addAll);
        return out;
    }

    public Collection<Event> all() { return List.copyOf(byId.values()); }
}
