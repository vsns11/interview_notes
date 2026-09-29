package lld.ch16.courseregistration;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/** What a rule may look at: who, which section, what they already have, and when. */
record RegistrationContext(Student student, Section section, Set<Section> currentSchedule, LocalDateTime now,
                           RegistrationWindow window) {}

/**
 * Specification-style rule: returns a reason when the request breaks it.
 * The registrar runs the rules as a chain and stops at the first violation.
 */
interface RegistrationRule { Optional<String> violation(RegistrationContext c); }

final class WindowRule implements RegistrationRule {
    public Optional<String> violation(RegistrationContext c) {
        return c.window().isOpenFor(c.student(), c.now()) ? Optional.empty()
                : Optional.of("registration window is closed for year " + c.student().year() + " at " + c.now());
    }
}

final class PrerequisiteRule implements RegistrationRule {
    public Optional<String> violation(RegistrationContext c) {
        var missing = c.section().course().prerequisites().stream()
                .filter(p -> !c.student().completedCourses().contains(p)).sorted().toList();
        return missing.isEmpty() ? Optional.empty() : Optional.of("missing prerequisites " + missing);
    }
}

final class DuplicateCourseRule implements RegistrationRule {
    public Optional<String> violation(RegistrationContext c) {
        return c.currentSchedule().stream()
                .filter(s -> s.course().code().equals(c.section().course().code())).findFirst()
                .map(s -> "already registered for " + s.course().code() + " in section " + s.id());
    }
}

final class CreditLimitRule implements RegistrationRule {
    public Optional<String> violation(RegistrationContext c) {
        int current = c.currentSchedule().stream().mapToInt(s -> s.course().credits()).sum();
        int after = current + c.section().course().credits();
        return after <= c.student().maxCredits() ? Optional.empty()
                : Optional.of("credit limit " + c.student().maxCredits() + " exceeded (" + after + ")");
    }
}

final class TimeConflictRule implements RegistrationRule {
    public Optional<String> violation(RegistrationContext c) {
        return c.currentSchedule().stream().filter(s -> s.slot().overlaps(c.section().slot())).findFirst()
                .map(s -> "time conflict with " + s.id() + " " + s.slot());
    }
}
