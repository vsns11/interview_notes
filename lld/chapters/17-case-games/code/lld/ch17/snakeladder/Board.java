package lld.ch17.snakeladder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Board configuration: size plus a map start -> Jump for O(1) lookup.
 * Validates the config once, up front, so the game loop can trust it.
 */
public final class Board {
    private final int size;
    private final Map<Integer, Jump> jumps = new HashMap<>();

    public Board(int size, Collection<Jump> all) {
        if (size < 10) throw new IllegalArgumentException("board too small");
        this.size = size;
        for (Jump j : all) {
            if (j.start() == j.end()) throw new IllegalArgumentException("jump to itself: " + j);
            if (j.start() <= 1 || j.start() >= size) throw new IllegalArgumentException("jump cannot start on first/last square: " + j);
            if (j.end() < 1 || j.end() > size) throw new IllegalArgumentException("jump ends off board: " + j);
            if (jumps.putIfAbsent(j.start(), j) != null) throw new IllegalArgumentException("two jumps start at " + j.start());
        }
        for (Jump j : jumps.values()) {   // no chains: an end square may not be another jump's start (also rules out loops)
            if (jumps.containsKey(j.end())) throw new IllegalArgumentException("jump " + j + " ends on another jump's start");
        }
    }

    public int size() { return size; }

    /** Returns the jump that starts on this square, or null. */
    public Jump jumpAt(int square) { return jumps.get(square); }
}
