package lld.ch01.playlist;

import java.util.List;

/**
 * Plays positions in an order decided once, up front. The order is injected
 * (not generated inside) so the program prints the same output every run: deterministic = testable.
 */
public final class Shuffled implements PlayOrder {
    private final List<Integer> positions;

    public Shuffled(List<Integer> positions) { this.positions = List.copyOf(positions); }

    @Override public int first(int size) { return positions.isEmpty() ? NONE : positions.get(0); }

    @Override public int after(int current, int size) {
        int i = positions.indexOf(current);
        return (i >= 0 && i + 1 < positions.size()) ? positions.get(i + 1) : NONE;
    }
}
