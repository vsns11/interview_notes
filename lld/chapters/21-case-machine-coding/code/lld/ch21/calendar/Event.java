package lld.ch21.calendar;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * One concrete occurrence. `attendees` is the resolved list of users that the event blocks
 * (direct users + the representatives chosen from each team).
 */
public record Event(String id, String name, LocalDate date, TimeSlot slot, Set<String> directUsers,
                    Set<String> teams, int representativesPerTeam, List<String> attendees, String seriesId) {
    public Event { attendees = List.copyOf(attendees); }
    public boolean blocks(String user) { return attendees.contains(user); }
    @Override public String toString() { return name + " " + date + " " + slot + " " + attendees; }
}
