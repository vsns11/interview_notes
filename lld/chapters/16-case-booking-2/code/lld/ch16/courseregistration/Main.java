package lld.ch16.courseregistration;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        var window = new RegistrationWindow(Map.of(
                4, LocalDateTime.of(2026, 11, 2, 9, 0),     // seniors first
                3, LocalDateTime.of(2026, 11, 3, 9, 0),
                2, LocalDateTime.of(2026, 11, 4, 9, 0),
                1, LocalDateTime.of(2026, 11, 5, 9, 0)),
                LocalDateTime.of(2026, 11, 20, 17, 0));
        var reg = new Registrar(window, msg -> System.out.println("    [notify] " + msg));

        var cs101 = new Course("CS101", "Intro to Programming", 4, Set.of());
        var cs201 = new Course("CS201", "Data Structures", 4, Set.of("CS101"));
        var math201 = new Course("MATH201", "Discrete Math", 3, Set.of());
        var cs301 = new Course("CS301", "Algorithms", 4, Set.of("CS201", "MATH201"));
        var drLee = new Instructor("I1", "Dr. Lee");
        var mw = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        var tt = EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
        reg.addSection(new Section("CS201-A", cs201, drLee, new TimeSlot(mw, LocalTime.of(10, 0), LocalTime.of(11, 30)), 2, 2));
        reg.addSection(new Section("CS201-B", cs201, drLee, new TimeSlot(tt, LocalTime.of(14, 0), LocalTime.of(15, 30)), 30, 5));
        reg.addSection(new Section("MATH201-A", math201, new Instructor("I2", "Dr. Rao"), new TimeSlot(mw, LocalTime.of(11, 0), LocalTime.of(12, 0)), 30, 5));
        reg.addSection(new Section("CS301-A", cs301, drLee, new TimeSlot(tt, LocalTime.of(9, 0), LocalTime.of(10, 30)), 30, 5));
        reg.addSection(new Section("CS101-A", cs101, new Instructor("I3", "Dr. Kim"), new TimeSlot(tt, LocalTime.of(11, 0), LocalTime.of(12, 0)), 30, 10));

        reg.addStudent(new Student("ana", "Ana", 4, Set.of("CS101"), 12));
        reg.addStudent(new Student("ben", "Ben", 3, Set.of("CS101"), 4));
        reg.addStudent(new Student("cy", "Cy", 2, Set.of(), 16));
        reg.addStudent(new Student("dee", "Dee", 3, Set.of("CS101"), 12));
        reg.addStudent(new Student("eve", "Eve", 3, Set.of("CS101"), 12));
        reg.addStudent(new Student("fay", "Fay", 3, Set.of("CS101"), 12));

        var nov2 = LocalDateTime.of(2026, 11, 2, 10, 0);
        var nov3 = LocalDateTime.of(2026, 11, 3, 10, 0);
        System.out.println("1) Search 'cs2': " + reg.search("cs2").stream().map(s -> s.id() + " " + s.slot()).toList());

        System.out.println("2) Rules");
        show("ben  CS201-A on Nov 2 ", reg.register("ben", "CS201-A", nov2));
        show("ana  CS201-A on Nov 2 ", reg.register("ana", "CS201-A", nov2));
        show("ana  MATH201-A        ", reg.register("ana", "MATH201-A", nov2));
        show("ana  CS201-B          ", reg.register("ana", "CS201-B", nov2));
        show("ana  CS301-A          ", reg.register("ana", "CS301-A", nov2));
        show("cy   CS201-B on Nov 4 ", reg.register("cy", "CS201-B", LocalDateTime.of(2026, 11, 4, 9, 30)));
        show("ana  after close      ", reg.register("ana", "CS201-B", LocalDateTime.of(2026, 11, 21, 9, 0)));

        System.out.println("3) Capacity 2 + waitlist 2 on CS201-A (juniors open Nov 3)");
        show("ben  CS201-A          ", reg.register("ben", "CS201-A", nov3));
        show("dee  CS201-A          ", reg.register("dee", "CS201-A", nov3));
        show("eve  CS201-A          ", reg.register("eve", "CS201-A", nov3));
        show("fay  CS201-A          ", reg.register("fay", "CS201-A", nov3));
        show("ben  MATH201-A        ", reg.register("ben", "MATH201-A", nov3));

        System.out.println("4) Ana drops CS201-A: the head of the waitlist is promoted");
        reg.drop("ana", "CS201-A");
        var a = reg.section("CS201-A");
        System.out.println("   roster " + a.roster() + ", waitlist " + a.waitlist());

        System.out.println("5) 50 first-years grab 30 seats (+10 waitlist) in CS101-A at 09:00 sharp");
        var opening = LocalDateTime.of(2026, 11, 5, 9, 0);
        for (int i = 0; i < 50; i++) reg.addStudent(new Student("fy" + i, "Fresher" + i, 1, Set.of(), 16));
        var counts = new ConcurrentHashMap<Outcome, Integer>();
        var go = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(16)) {
            for (int i = 0; i < 50; i++) {
                String id = "fy" + i;
                pool.submit(() -> {
                    go.await();
                    counts.merge(reg.register(id, "CS101-A", opening).outcome(), 1, Integer::sum);
                    return null;
                });
            }
            go.countDown();
        }
        System.out.println("   ENROLLED=" + counts.get(Outcome.ENROLLED) + " WAITLISTED=" + counts.get(Outcome.WAITLISTED)
                + " REJECTED=" + counts.get(Outcome.REJECTED) + ", roster size " + reg.section("CS101-A").roster().size());

        System.out.println("6) One student double-clicks: two threads, same request");
        reg.addStudent(new Student("gus", "Gus", 3, Set.of("CS101"), 12));
        var outcomes = new ConcurrentHashMap<Outcome, Integer>();
        try (var pool = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++)
                pool.submit(() -> outcomes.merge(reg.register("gus", "CS201-B", nov3).outcome(), 1, Integer::sum));
        }
        System.out.println("   " + new java.util.TreeMap<>(outcomes) + ", gus schedule " + reg.schedule("gus").stream().map(Section::id).toList());
    }

    static void show(String label, RegistrationResult r) {
        System.out.println("   " + label + " -> " + r.outcome() + ": " + r.message());
    }
}
