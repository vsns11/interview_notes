package lld.ch16.courseregistration;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** A catalog entry: what is taught. Prerequisites are course codes. */
record Course(String code, String title, int credits, Set<String> prerequisites) {
    Course { prerequisites = Set.copyOf(prerequisites); }
}

record Instructor(String id, String name) {}

/** When a section meets, e.g. Mon+Wed 10:00-11:30. */
record TimeSlot(Set<DayOfWeek> days, LocalTime start, LocalTime end) {
    TimeSlot {
        days = EnumSet.copyOf(days);
        if (!end.isAfter(start)) throw new IllegalArgumentException("end must be after start");
    }
    boolean overlaps(TimeSlot o) {
        boolean sharedDay = days.stream().anyMatch(o.days::contains);
        return sharedDay && start.isBefore(o.end) && o.start.isBefore(end);
    }
    @Override public String toString() { return days + " " + start + "-" + end; }
}

record Student(String id, String name, int year, Set<String> completedCourses, int maxCredits) {
    Student { completedCourses = Set.copyOf(completedCourses); }
}

/** Priority registration: each class year opens at its own time; everyone closes together. */
record RegistrationWindow(Map<Integer, LocalDateTime> opensByYear, LocalDateTime closes) {
    boolean isOpenFor(Student s, LocalDateTime now) {
        var opens = opensByYear.get(s.year());
        return opens != null && !now.isBefore(opens) && now.isBefore(closes);
    }
}

enum Outcome { ENROLLED, WAITLISTED, REJECTED }

record RegistrationResult(Outcome outcome, String message) {
    static RegistrationResult rejected(String why) { return new RegistrationResult(Outcome.REJECTED, why); }
}
