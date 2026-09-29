package lld.ch03.composition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Composition (filled diamond at ParkingFloor): ParkingFloor 1 *-- 1..* ParkingSpot.
 * The floor CREATES its spots in its own constructor, never shares them with another floor,
 * and when the floor is removed the spots go with it.
 */
public final class ParkingFloor {
    private final int level;
    private final List<ParkingSpot> spots = new ArrayList<>();

    ParkingFloor(int level, Map<SpotType, Integer> layout) {
        this.level = level;
        int n = 1;
        for (var e : layout.entrySet()) {
            for (int i = 0; i < e.getValue(); i++) {
                spots.add(new ParkingSpot("L" + level + "-" + n++, e.getKey()));
            }
        }
        if (spots.isEmpty()) throw new IllegalArgumentException("a floor needs 1..* spots");
    }

    public int level() { return level; }
    public List<ParkingSpot> spots() { return Collections.unmodifiableList(spots); }

    public long freeSpots() { return spots.stream().filter(ParkingSpot::isFree).count(); }

    /** Parks in the first free spot of the type; the spot itself never leaves the floor. */
    public String park(SpotType type) {
        for (ParkingSpot s : spots) {
            if (s.isFree() && s.type() == type) { s.occupy(); return s.id(); }
        }
        return null;
    }
}
