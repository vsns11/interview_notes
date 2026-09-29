package lld.ch16.courseregistration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The service. Lock order is ALWAYS student-then-section, which rules out deadlock:
 *  - the student lock makes "check my credits/time clashes, then take the seat" atomic per student
 *    (stops one student double-registering from two browser tabs);
 *  - the section lock (inside Section) makes the seat count atomic across students.
 */
final class Registrar {
    private final Map<String, Student> students = new ConcurrentHashMap<>();
    private final Map<String, Section> sections = new ConcurrentHashMap<>();
    private final Map<String, Set<Section>> schedules = new ConcurrentHashMap<>();
    private final RegistrationWindow window;
    private final List<RegistrationRule> fullChecks;
    private final List<RegistrationRule> promotionChecks;
    private final Consumer<String> notifier;

    Registrar(RegistrationWindow window, Consumer<String> notifier) {
        this.window = window;
        this.notifier = notifier;
        this.fullChecks = List.of(new WindowRule(), new PrerequisiteRule(), new DuplicateCourseRule(),
                new CreditLimitRule(), new TimeConflictRule());
        this.promotionChecks = List.of(new DuplicateCourseRule(), new CreditLimitRule(), new TimeConflictRule());
    }

    void addStudent(Student s) { students.put(s.id(), s); schedules.put(s.id(), ConcurrentHashMap.newKeySet()); }
    void addSection(Section s) { sections.put(s.id(), s); }
    Section section(String id) { return sections.get(id); }

    List<Section> search(String text) {
        var q = text.toLowerCase();
        return sections.values().stream().filter(s -> s.course().code().toLowerCase().contains(q)
                || s.course().title().toLowerCase().contains(q)).sorted((a, b) -> a.id().compareTo(b.id())).toList();
    }

    Set<Section> schedule(String studentId) { return Set.copyOf(schedules.get(studentId)); }

    private Optional<String> firstViolation(List<RegistrationRule> rules, RegistrationContext ctx) {
        for (var r : rules) {
            var v = r.violation(ctx);
            if (v.isPresent()) return v;
        }
        return Optional.empty();
    }

    RegistrationResult register(String studentId, String sectionId, LocalDateTime now) {
        var student = students.get(studentId);
        var section = sections.get(sectionId);
        if (student == null || section == null) return RegistrationResult.rejected("unknown student or section");
        var schedule = schedules.get(studentId);
        synchronized (schedule) {                                    // 1st lock: the student
            var ctx = new RegistrationContext(student, section, Set.copyOf(schedule), now, window);
            var v = firstViolation(fullChecks, ctx);
            if (v.isPresent()) return RegistrationResult.rejected(v.get());
            var result = section.enroll(studentId);                  // 2nd lock: the section
            if (result.outcome() == Outcome.ENROLLED) schedule.add(section);
            return result;
        }
    }

    void drop(String studentId, String sectionId) {
        var section = sections.get(sectionId);
        var schedule = schedules.get(studentId);
        Optional<String> promoted;
        synchronized (schedule) {
            promoted = section.drop(studentId);
            schedule.remove(section);
        }
        notifier.accept(studentId + " dropped " + sectionId);
        // Finish promotions OUTSIDE the dropper's lock, one student lock at a time (no nested student locks).
        while (promoted.isPresent()) promoted = finishPromotion(promoted.get(), section);
    }

    /** The promoted student's timetable may have changed while waiting, so re-check. Returns the next promotion, if any. */
    private Optional<String> finishPromotion(String studentId, Section section) {
        var schedule = schedules.get(studentId);
        synchronized (schedule) {
            var ctx = new RegistrationContext(students.get(studentId), section, Set.copyOf(schedule), null, window);
            var v = firstViolation(promotionChecks, ctx);
            if (v.isEmpty()) {
                schedule.add(section);
                notifier.accept(studentId + " promoted from waitlist into " + section.id());
                return Optional.empty();
            }
            notifier.accept(studentId + " skipped for " + section.id() + ": " + v.get());
            return section.drop(studentId);
        }
    }
}
