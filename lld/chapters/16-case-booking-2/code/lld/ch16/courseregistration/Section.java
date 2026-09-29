package lld.ch16.courseregistration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One offering of a course (CS201 section A). Capacity and waitlist live HERE and are
 * guarded by the section's own lock, so thousands of students can hit different sections in parallel.
 */
final class Section {
    private final String id;
    private final Course course;
    private final Instructor instructor;
    private final TimeSlot slot;
    private final int capacity;
    private final int waitlistCapacity;
    private final Set<String> enrolled = new LinkedHashSet<>();
    private final Deque<String> waitlist = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    Section(String id, Course course, Instructor instructor, TimeSlot slot, int capacity, int waitlistCapacity) {
        this.id = id; this.course = course; this.instructor = instructor; this.slot = slot;
        this.capacity = capacity; this.waitlistCapacity = waitlistCapacity;
    }

    String id() { return id; }
    Course course() { return course; }
    Instructor instructor() { return instructor; }
    TimeSlot slot() { return slot; }

    /** The only place a seat is taken: check-and-add in one critical section. */
    RegistrationResult enroll(String studentId) {
        lock.lock();
        try {
            if (enrolled.contains(studentId)) return RegistrationResult.rejected("already enrolled in " + id);
            if (waitlist.contains(studentId)) return RegistrationResult.rejected("already on the waitlist of " + id);
            if (enrolled.size() < capacity) {
                enrolled.add(studentId);
                return new RegistrationResult(Outcome.ENROLLED, "enrolled in " + id + " (" + enrolled.size() + "/" + capacity + ")");
            }
            if (waitlist.size() < waitlistCapacity) {
                waitlist.addLast(studentId);
                return new RegistrationResult(Outcome.WAITLISTED, "waitlisted for " + id + " at position " + waitlist.size());
            }
            return RegistrationResult.rejected(id + " and its waitlist are full");
        } finally {
            lock.unlock();
        }
    }

    /** Removes the student; if a seat opened, moves the head of the waitlist in and returns who it was. */
    Optional<String> drop(String studentId) {
        lock.lock();
        try {
            if (waitlist.remove(studentId)) return Optional.empty();
            if (!enrolled.remove(studentId)) throw new IllegalArgumentException(studentId + " is not in " + id);
            String next = waitlist.pollFirst();
            if (next != null) enrolled.add(next);
            return Optional.ofNullable(next);
        } finally {
            lock.unlock();
        }
    }

    boolean isEnrolled(String studentId) {
        lock.lock();
        try { return enrolled.contains(studentId); } finally { lock.unlock(); }
    }

    List<String> roster() {
        lock.lock();
        try { return List.copyOf(enrolled); } finally { lock.unlock(); }
    }

    List<String> waitlist() {
        lock.lock();
        try { return List.copyOf(waitlist); } finally { lock.unlock(); }
    }
}
