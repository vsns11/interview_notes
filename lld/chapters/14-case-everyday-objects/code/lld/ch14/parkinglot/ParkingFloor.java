package lld.ch14.parkinglot;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observer: anything that wants to know when free-spot counts change. */
interface AvailabilityListener {
    void onAvailabilityChanged(int floor, SpotType type, int freeNow);
}

/** A floor keeps one sorted, thread-safe set of FREE spots per spot type. */
class ParkingFloor {
    private final int number;
    private final Map<SpotType, ConcurrentSkipListSet<ParkingSpot>> free = new EnumMap<>(SpotType.class);
    private final List<AvailabilityListener> listeners = new CopyOnWriteArrayList<>();
    private final Set<SpotType> typesHere;

    ParkingFloor(int number, Map<SpotType, Integer> spotCounts) {
        this.number = number;
        for (SpotType t : SpotType.values()) free.put(t, new ConcurrentSkipListSet<>());
        typesHere = Set.copyOf(spotCounts.keySet());
        spotCounts.forEach((type, count) -> {
            for (int i = 1; i <= count; i++) free.get(type).add(SpotFactory.create(number, type, i));
        });
    }

    void addListener(AvailabilityListener l) {
        listeners.add(l);
        for (SpotType t : typesHere) l.onAvailabilityChanged(number, t, freeCount(t));
    }

    /** pollFirst() is atomic: two threads can never receive the same spot. Lowest id = nearest. */
    Optional<ParkingSpot> tryPark(Vehicle v, SpotType type) {
        ParkingSpot spot = free.get(type).pollFirst();
        if (spot == null) return Optional.empty();
        if (!spot.tryOccupy(v)) throw new IllegalStateException("free-set out of sync for " + spot);
        notifyListeners(type);
        return Optional.of(spot);
    }

    void release(ParkingSpot spot) {
        spot.release();
        free.get(spot.type()).add(spot);
        notifyListeners(spot.type());
    }

    int freeCount(SpotType t) { return free.get(t).size(); }
    int number() { return number; }

    private void notifyListeners(SpotType t) {
        int n = freeCount(t);
        for (AvailabilityListener l : listeners) l.onAvailabilityChanged(number, t, n);
    }
}

/** The display board on a floor: shows free spots per type. */
class DisplayBoard implements AvailabilityListener {
    private final Map<SpotType, Integer> shown = new EnumMap<>(SpotType.class);

    @Override public synchronized void onAvailabilityChanged(int floor, SpotType type, int freeNow) {
        shown.put(type, freeNow);
    }

    synchronized String render() {
        StringBuilder sb = new StringBuilder();
        shown.forEach((t, n) -> sb.append(t).append('=').append(n == 0 ? "FULL" : n).append(' '));
        return sb.toString().trim();
    }
}
