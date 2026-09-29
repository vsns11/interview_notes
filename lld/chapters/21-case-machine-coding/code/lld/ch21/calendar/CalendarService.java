package lld.ch21.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CalendarService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Team> teams = new ConcurrentHashMap<>();
    private final Map<String, String> teamOf = new ConcurrentHashMap<>();   // user -> team
    private final EventStore store = new EventStore();
    private final RepresentativeSelector selector;
    private final AtomicInteger seq = new AtomicInteger();

    public CalendarService(RepresentativeSelector selector) { this.selector = selector; }

    public User createUser(String name, LocalTime workStart, LocalTime workEnd) {
        User u = new User(name, new TimeSlot(workStart, workEnd));
        if (users.putIfAbsent(name, u) != null) throw new CalendarException("User already exists: " + name);
        return u;
    }

    public synchronized Team createTeam(String name, String... memberNames) {
        if (teams.containsKey(name)) throw new CalendarException("Team already exists: " + name);
        List<User> members = new ArrayList<>();
        for (String m : memberNames) {
            User u = user(m);
            if (teamOf.containsKey(m)) throw new CalendarException(m + " already belongs to team " + teamOf.get(m));
            members.add(u);
        }
        members.forEach(u -> teamOf.put(u.name(), name));
        Team t = new Team(name, members);
        teams.put(name, t);
        return t;
    }

    /** synchronized: the check-then-book sequence must be atomic or two events could grab the same person. */
    public synchronized Event createEvent(String name, Set<String> userNames, Set<String> teamNames,
                                          int reps, LocalDate date, TimeSlot slot) {
        Event e = plan(name, userNames, teamNames, reps, date, slot, null, null);
        store.save(e);
        return e;
    }

    /** Bonus: recurring event. All occurrences are validated first; nothing is saved unless all fit. */
    public synchronized List<Event> createRecurring(String name, Set<String> userNames, Set<String> teamNames, int reps,
                                                    TimeSlot slot, LocalDate from, LocalDate to, Frequency freq) {
        String series = "S" + seq.incrementAndGet();
        List<Event> planned = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = freq.next(d)) {
            planned.add(plan(name + "@" + d, userNames, teamNames, reps, d, slot, series, null));
        }
        planned.forEach(store::save);
        return planned;
    }

    /** Bonus: move an event. We try the new time ignoring the event itself; on failure nothing changes. */
    public synchronized Event updateTime(String eventId, LocalDate date, TimeSlot slot) {
        Event old = store.find(eventId).orElseThrow(() -> new CalendarException("No such event " + eventId));
        Event moved = plan(old.name(), old.directUsers(), old.teams(), old.representativesPerTeam(),
                date, slot, old.seriesId(), old);          // keeps old.id(); throws if the new time does not fit
        store.remove(old);
        store.save(moved);
        return moved;
    }

    /** Events a user is blocked for between two date-times (includes team-representative bookings). */
    public synchronized List<Event> eventsFor(String userName, LocalDateTime from, LocalDateTime to) {
        user(userName);
        return store.between(userName, from.toLocalDate(), to.toLocalDate()).stream()
                .filter(e -> LocalDateTime.of(e.date(), e.slot().end()).isAfter(from)
                          && LocalDateTime.of(e.date(), e.slot().start()).isBefore(to))
                .sorted(Comparator.comparing(Event::date).thenComparing(e -> e.slot().start()))
                .toList();
    }

    /**
     * Free slots on a day where every listed user is free AND each listed team has at least `reps`
     * free members. Sweep: cut the day at every boundary (working-hour edges, event edges),
     * test each small piece, then merge adjacent good pieces.
     */
    public synchronized List<TimeSlot> freeSlots(Set<String> userNames, Set<String> teamNames, int reps, LocalDate date) {
        List<User> direct = userNames.stream().map(this::user).toList();
        List<Team> tms = teamNames.stream().map(this::team).toList();
        List<User> everyone = new ArrayList<>(direct);
        tms.forEach(t -> everyone.addAll(t.members()));

        TreeSet<LocalTime> cuts = new TreeSet<>();
        for (User u : everyone) {
            cuts.add(u.workingHours().start());
            cuts.add(u.workingHours().end());
            for (Event e : store.onDay(u.name(), date)) { cuts.add(e.slot().start()); cuts.add(e.slot().end()); }
        }
        List<TimeSlot> result = new ArrayList<>();
        LocalTime prev = null;
        for (LocalTime cut : cuts) {
            if (prev != null) {
                TimeSlot piece = new TimeSlot(prev, cut);
                boolean ok = direct.stream().allMatch(u -> isFree(u, date, piece, null))
                        && tms.stream().allMatch(t -> t.members().stream()
                               .filter(m -> isFree(m, date, piece, null)).count() >= reps);
                if (ok) {
                    int last = result.size() - 1;
                    if (last >= 0 && result.get(last).end().equals(piece.start()))
                        result.set(last, new TimeSlot(result.get(last).start(), piece.end()));
                    else result.add(piece);
                }
            }
            prev = cut;
        }
        return result;
    }

    // ---------------------------------------------------------------- helpers

    private Event plan(String name, Set<String> userNames, Set<String> teamNames, int reps,
                       LocalDate date, TimeSlot slot, String series, Event ignore) {
        if (userNames.isEmpty() && teamNames.isEmpty()) throw new CalendarException("Event needs participants");
        if (!teamNames.isEmpty() && reps < 1) throw new CalendarException("Representatives must be >= 1");
        Set<String> attendees = new LinkedHashSet<>();
        for (String n : new TreeSet<>(userNames)) {          // sorted => deterministic order
            User u = user(n);
            if (!isFree(u, date, slot, ignore))
                throw new CalendarException(name + ": " + n + " is not available " + date + " " + slot);
            attendees.add(n);
        }
        for (String tn : new TreeSet<>(teamNames)) {
            Team t = team(tn);
            List<User> chosen = selector.choose(t, reps,
                    m -> !attendees.contains(m.name()) && isFree(m, date, slot, ignore));
            if (chosen.size() < reps)
                throw new CalendarException(name + ": team " + tn + " has only " + chosen.size()
                        + " free member(s), needs " + reps);
            chosen.forEach(m -> attendees.add(m.name()));
        }
        String id = ignore != null ? ignore.id() : "E" + seq.incrementAndGet();
        return new Event(id, name, date, slot, Set.copyOf(userNames), Set.copyOf(teamNames), reps,
                List.copyOf(attendees), series);
    }

    private boolean isFree(User u, LocalDate date, TimeSlot slot, Event ignore) {
        if (!u.workingHours().contains(slot)) return false;
        return store.onDay(u.name(), date).stream()
                .filter(e -> ignore == null || !e.id().equals(ignore.id()))
                .noneMatch(e -> e.slot().overlaps(slot));
    }

    private User user(String n) {
        User u = users.get(n);
        if (u == null) throw new CalendarException("Unknown user " + n);
        return u;
    }

    private Team team(String n) {
        Team t = teams.get(n);
        if (t == null) throw new CalendarException("Unknown team " + n);
        return t;
    }

    public synchronized Collection<Event> allEvents() { return store.all(); }
}
