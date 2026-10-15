package lld.ch16.meetingscheduler;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        var inbox = new InboxNotifier();
        var counter = new EventCounter();
        var s = new MeetingScheduler(new SmallestFitStrategy());
        s.subscribe(inbox);
        s.subscribe(counter);

        s.addRoom(new Room("R1", "Huddle", 4, EnumSet.of(Equipment.WHITEBOARD)));
        s.addRoom(new Room("R2", "Maple", 8, EnumSet.of(Equipment.VIDEO_CONFERENCE, Equipment.WHITEBOARD)));
        s.addRoom(new Room("R3", "Oak", 8, EnumSet.of(Equipment.PROJECTOR)));
        s.addRoom(new Room("R4", "Atrium", 20, EnumSet.of(Equipment.VIDEO_CONFERENCE, Equipment.PROJECTOR)));

        var ana = new User("ana", "Ana"); var ben = new User("ben", "Ben"); var cy = new User("cy", "Cy");
        var dee = new User("dee", "Dee"); var eve = new User("eve", "Eve");
        LocalDateTime mon = LocalDateTime.of(2026, 11, 2, 0, 0);     // a Monday

        System.out.println("1) Ana books a 5-person video call; the scheduler picks the smallest room that fits");
        var r1 = s.book(MeetingRequest.auto(ana, "Design review", Interval.of(mon.withHour(10), 60),
                List.of(ben, cy, dee, eve), Set.of(Equipment.VIDEO_CONFERENCE)));
        show(r1);
        String m1 = r1.meeting().id();

        System.out.println("2) Conflicts in a named room (half-open intervals)");
        show(s.book(MeetingRequest.inRoom(ben, "1:1", Interval.of(mon.withHour(10).withMinute(30), 60), List.of(cy), "R2")));
        show(s.book(MeetingRequest.inRoom(ben, "Back-to-back", Interval.of(mon.withHour(11), 30), List.of(cy), "R2")));

        System.out.println("3) Capacity");
        var twelve = List.of(ben, cy, dee, eve, u("f1"), u("f2"), u("f3"), u("f4"), u("f5"), u("f6"), u("f7"));
        inbox.echo(false);
        show(s.book(MeetingRequest.inRoom(ana, "All hands", Interval.of(mon.withHour(15), 60), twelve, "R1")));
        show(s.book(MeetingRequest.auto(ana, "All hands", Interval.of(mon.withHour(15), 60), twelve, Set.of())));
        inbox.echo(true);

        System.out.println("4) RSVPs go back to the organizer");
        s.respond(m1, "ben", RsvpStatus.ACCEPTED);
        s.respond(m1, "cy", RsvpStatus.DECLINED);
        s.respond(m1, "dee", RsvpStatus.TENTATIVE);
        System.out.println("   " + m1 + " " + s.meeting(m1).rsvpSummary());

        System.out.println("5) Soft conflict: Eve is double-booked, which is allowed but reported");
        show(s.book(MeetingRequest.auto(dee, "Hiring sync", Interval.of(mon.withHour(10).withMinute(30), 30), List.of(eve), Set.of())));
        show(s.book(MeetingRequest.auto(dee, "Coffee", Interval.of(mon.withHour(10).withMinute(30), 30), List.of(cy), Set.of())));

        System.out.println("6) Ana moves the design review to 14:00: same room, RSVPs reset");
        show(s.reschedule(m1, "ana", Interval.of(mon.withHour(14), 60)));
        System.out.println("   " + m1 + " " + s.meeting(m1).rsvpSummary());
        try { s.cancel(m1, "ben"); } catch (SecurityException e) { System.out.println("   ben cancels -> " + e.getMessage()); }

        System.out.println("7) Ana cancels; the slot is free again");
        s.cancel(m1, "ana");
        s.cancel(m1, "ana");                                           // idempotent: no second round of messages
        show(s.book(MeetingRequest.inRoom(ben, "Retro", Interval.of(mon.withHour(14), 60), List.of(cy, dee), "R2")));

        System.out.println("8) Recurring weekly stand-up x4 in Huddle; a later series must be all-or-nothing");
        inbox.echo(false);
        var standup = MeetingRequest.inRoom(cy, "Stand-up", Interval.of(mon.withHour(9).withMinute(30), 15), List.of(dee, eve), "R1");
        var series = s.bookRecurring(standup, new Recurrence(Frequency.WEEKLY, 4));
        show(series);
        series.meetings().forEach(m -> System.out.println("     " + m));
        var clash = MeetingRequest.inRoom(ben, "Planning", Interval.of(mon.plusWeeks(2).withHour(9), 60), List.of(cy), "R1");
        show(s.bookRecurring(clash, new Recurrence(Frequency.DAILY, 3)));
        var auto = MeetingRequest.auto(ben, "Planning", Interval.of(mon.plusWeeks(2).withHour(9), 60), List.of(cy), Set.of());
        show(s.bookRecurring(auto, new Recurrence(Frequency.DAILY, 3)));
        s.cancelSeries(series.meeting().seriesId(), "cy");
        System.out.println("   after cancelSeries, Huddle bookings: " + s.room("R1").bookings().size());

        System.out.println("9) Race: 20 organizers want Oak at Tue 10:00 at the same instant");
        var tue10 = Interval.of(mon.plusDays(1).withHour(10), 60);
        var wins = new AtomicInteger();
        race(20, i -> { if (s.book(MeetingRequest.inRoom(u("o" + i), "Race", tue10, List.of(), "R3")).ok()) wins.incrementAndGet(); });
        System.out.println("   winners=" + wins + ", Oak bookings at that time="
                + s.room("R3").bookings().stream().filter(b -> b.interval().overlaps(tue10)).count());

        System.out.println("10) Race: 20 organizers, any room, Wed 10:00 - 4 rooms exist, so exactly 4 win");
        var wed10 = Interval.of(mon.plusDays(2).withHour(10), 60);
        var byRoom = new ConcurrentHashMap<String, Integer>();
        var lost = new AtomicInteger();
        race(20, i -> {
            var r = s.book(MeetingRequest.auto(u("p" + i), "Race", wed10, List.of(), Set.of()));
            if (r.ok()) byRoom.merge(r.meeting().room().name(), 1, Integer::sum); else lost.incrementAndGet();
        });
        System.out.println("   per room " + new TreeMap<>(byRoom) + ", rejected=" + lost);

        System.out.println("11) Inboxes and event counts");
        System.out.println("   eve's inbox: " + inbox.inbox("eve").size() + " messages, first: " + inbox.inbox("eve").get(0));
        System.out.println("   ana's inbox: " + inbox.inbox("ana"));
        System.out.println("   events " + counter);
        System.out.println("   ben's Monday: " + s.agenda("ben", mon.toLocalDate()));
    }

    static User u(String id) { return new User(id, id); }

    interface Task { void run(int i) throws Exception; }

    /** Starts n tasks on a pool, releases them together with a latch, and waits for all of them. */
    static void race(int n, Task t) throws Exception {
        var go = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(n)) {
            for (int i = 0; i < n; i++) {
                int k = i;
                pool.submit(() -> { go.await(); t.run(k); return null; });
            }
            go.countDown();
        }
    }

    static void show(BookingResult r) {
        System.out.println("   " + (r.ok() ? "OK   " : "FAIL ") + r.message() + (r.warnings().isEmpty() ? "" : " warnings=" + r.warnings()));
    }
}
