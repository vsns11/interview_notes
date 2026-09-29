package lld.ch21.taskmgmt;

import java.time.LocalDateTime;

/** One line of a task's audit history ("who did what, when"). */
public record Activity(LocalDateTime at, String actorId, String description) {
    @Override public String toString() { return at.toLocalTime() + " " + actorId + ": " + description; }
}
