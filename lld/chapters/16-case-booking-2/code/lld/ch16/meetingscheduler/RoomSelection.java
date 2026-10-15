package lld.ch16.meetingscheduler;

import java.util.Comparator;
import java.util.List;

/**
 * Strategy: given the rooms that physically fit, decide the order to try them in.
 * The scheduler then tries to claim them in that order, so ranking never has to be "right" about
 * availability - a room taken a moment ago just fails its claim and the next one is tried.
 */
interface RoomSelectionStrategy {
    List<Room> rank(List<Room> fitting, MeetingRequest request);
}

/** Default: the smallest room that fits, so the big rooms stay free for big meetings. Ties by id. */
final class SmallestFitStrategy implements RoomSelectionStrategy {
    @Override public List<Room> rank(List<Room> fitting, MeetingRequest r) {
        return fitting.stream().sorted(Comparator.comparingInt(Room::capacity).thenComparing(Room::id)).toList();
    }
}

/** Alternative: spread load - the room with the fewest bookings that day first (cleaning, wear, noise). */
final class LeastBusyStrategy implements RoomSelectionStrategy {
    @Override public List<Room> rank(List<Room> fitting, MeetingRequest r) {
        var day = r.when().start().toLocalDate();
        return fitting.stream()
                .sorted(Comparator.comparingInt((Room room) -> room.bookingsOn(day)).thenComparingInt(Room::capacity).thenComparing(Room::id))
                .toList();
    }
}
