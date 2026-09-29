package lld.ch21.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Replays the sample test cases from the problem statement, then the bonus features. */
public class Main {
    public static void main(String[] args) {
        CalendarService cal = new CalendarService(RepresentativeSelector.firstAvailable());
        LocalDate today = LocalDate.of(2026, 9, 28), tomorrow = today.plusDays(1);

        System.out.println("== 1. onboard users");
        cal.createUser("A", t("10:00"), t("19:00"));
        cal.createUser("B", t("09:30"), t("17:30"));
        cal.createUser("C", t("11:30"), t("18:30"));
        cal.createUser("D", t("10:00"), t("18:00"));
        cal.createUser("E", t("11:00"), t("19:30"));
        cal.createUser("F", t("11:00"), t("18:30"));
        System.out.println("  6 users created");

        System.out.println("== 2. teams");
        cal.createTeam("T1", "C", "E");
        cal.createTeam("T2", "B", "D", "F");
        System.out.println("  T1=[C,E] T2=[B,D,F]");
        attempt(() -> cal.createTeam("T3", "A", "C"));

        System.out.println("== 3. events");
        attempt(() -> cal.createEvent("Event1", Set.of("A"), Set.of("T1"), 2, tomorrow, slot("14:00", "15:00")));
        attempt(() -> cal.createEvent("Event2", Set.of("C"), Set.of(), 0, tomorrow, slot("14:00", "15:00")));
        attempt(() -> cal.createEvent("Event3", Set.of(), Set.of("T1", "T2"), 2, today, slot("15:00", "16:00")));
        attempt(() -> cal.createEvent("Event4", Set.of("A"), Set.of("T2"), 1, today, slot("15:00", "16:00")));
        attempt(() -> cal.createEvent("Event5", Set.of("A", "F"), Set.of(), 0, today, slot("10:00", "11:00")));

        System.out.println("== 4. events in a time range (today 10:00 .. tomorrow 17:00)");
        LocalDateTime from = LocalDateTime.of(today, t("10:00")), to = LocalDateTime.of(tomorrow, t("17:00"));
        for (String u : List.of("A", "B", "F"))
            System.out.println("  " + u + ": " + cal.eventsFor(u, from, to).stream().map(Event::name).toList());

        System.out.println("== 5. free slots today for A + T1 (1 representative)");
        System.out.println("  " + cal.freeSlots(Set.of("A"), Set.of("T1"), 1, today));
        System.out.println("  free slots today for B + D: " + cal.freeSlots(Set.of("B", "D"), Set.of(), 0, today));

        System.out.println("== bonus: update event time");
        Event e1 = cal.allEvents().stream().filter(e -> e.name().equals("Event1")).findFirst().orElseThrow();
        attempt(() -> cal.updateTime(e1.id(), tomorrow, slot("18:30", "19:00")));   // needs 2 of T1, but C stops at 18:30
        attempt(() -> cal.updateTime(e1.id(), tomorrow, slot("16:00", "17:00")));

        System.out.println("== bonus: recurring daily stand-up (A + D), 3 days");
        attempt(() -> cal.createRecurring("Standup", Set.of("A", "D"), Set.of(), 0, slot("10:00", "10:15"),
                today, today.plusDays(2), Frequency.DAILY));
        attempt(() -> cal.createRecurring("Sync", Set.of("A"), Set.of(), 0, slot("15:30", "16:30"),
                today, today.plusDays(2), Frequency.DAILY));   // clashes with Event4 today -> whole series rejected
        System.out.println("  A today..+2: " + cal.eventsFor("A", LocalDateTime.of(today, t("00:00")),
                LocalDateTime.of(today.plusDays(2), t("23:59"))).stream().map(Event::name).toList());
    }

    static LocalTime t(String s) { return LocalTime.parse(s); }
    static TimeSlot slot(String a, String b) { return new TimeSlot(t(a), t(b)); }

    static void attempt(Supplier<?> s) {
        try { System.out.println("  ok: " + s.get()); }
        catch (RuntimeException e) { System.out.println("  FAILED: " + e.getMessage()); }
    }
}
